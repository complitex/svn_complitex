/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;

import org.complitex.osznconnection.file.entity.ConfigName;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс для запуска потоков на связывание и обработку payment and benefit файлов.
 * @author Artem
 */
public class FileExecutorService {

    private static final Logger log = LoggerFactory.getLogger(FileExecutorService.class);

    private static FileExecutorService fileExecutorService;

    private List<RequestFile> inBinding = Collections.synchronizedList(new ArrayList<RequestFile>());

    private List<RequestFile> inProcessing = Collections.synchronizedList(new ArrayList<RequestFile>());

    private AtomicInteger bindingCounter = new AtomicInteger(0);

    private AtomicInteger processingCounter = new AtomicInteger(0);

    private FileExecutorService() {
    }

    public synchronized static FileExecutorService get() {
        if (fileExecutorService == null) {
            fileExecutorService = new FileExecutorService();
        }
        return fileExecutorService;
    }

    private ExecutorService bindingThreadPool = initBindingThreadPool();

    private static ExecutorService initBindingThreadPool() {
        return Executors.newFixedThreadPool(ConfigStatic.get().getInteger(ConfigName.BINDING_THREAD_SIZE, true));
    }

    public boolean isBinding() {
        return bindingCounter.get() > 0;
    }

    private ExecutorService processingThreadPool = initProcessingThreadPool();

    private static ExecutorService initProcessingThreadPool() {
        return Executors.newFixedThreadPool(ConfigStatic.get().getInteger(ConfigName.PROCESSING_THREAD_SIZE, true));
    }

    public boolean isProcessing() {
        return processingCounter.get() > 0;
    }

    public List<RequestFile> getInBinding(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(inBinding);

        if (flush) {
            inBinding.clear();
        }

        return Collections.unmodifiableList(list);
    }

    public List<RequestFile> getInProcessing(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(inProcessing);

        if (flush) {
            inProcessing.clear();
        }

        return Collections.unmodifiableList(list);
    }

    private class BindTask implements Runnable {

        private RequestFile paymentFile;

        private RequestFile benefitFile;

        public BindTask(RequestFile paymentFile, RequestFile benefitFile) {
            this.paymentFile = paymentFile;
            this.benefitFile = benefitFile;
        }

        @Override
        public void run() {
            try {
                bindingCounter.incrementAndGet();

                BindingRequestBean bindingRequestBean = getBindingBean();

                if (paymentFile != null && benefitFile != null) {
                    inBinding.add(paymentFile);
                    inBinding.add(benefitFile);
                    try {
                        bindingRequestBean.bindPaymentAndBenefit(paymentFile, benefitFile);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

            } catch (Exception e) {
                log.error("", e);
            } finally {
                bindingCounter.decrementAndGet();
            }
        }

        private BindingRequestBean getBindingBean() {
            try {
                InitialContext context = new InitialContext();
                return (BindingRequestBean) context.lookup("java:module/" + BindingRequestBean.class.getSimpleName());
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ProcessTask implements Runnable {

        private RequestFile paymentFile;

        private RequestFile benefitFile;

        public ProcessTask(RequestFile paymentFile, RequestFile benefitFile) {
            this.paymentFile = paymentFile;
            this.benefitFile = benefitFile;
        }

        @Override
        public void run() {
            try {
                processingCounter.incrementAndGet();

                ProcessingRequestBean processRequestBean = getProcessBean();
                if (paymentFile != null && benefitFile != null) {
                    inProcessing.add(paymentFile);
                    inProcessing.add(benefitFile);
                    try {
                        processRequestBean.processPaymentAndBenefit(paymentFile, benefitFile);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            } finally {
                processingCounter.decrementAndGet();
            }
        }

        private ProcessingRequestBean getProcessBean() {
            try {
                InitialContext context = new InitialContext();
                return (ProcessingRequestBean) context.lookup("java:module/" + ProcessingRequestBean.class.getSimpleName());
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Запускает процесс связывания файлов.
     * @param requestFiles
     */
    public void bind(List<RequestFile> requestFiles) {
        List<RequestFile> suitedFiles = Lists.newArrayList(Iterables.filter(requestFiles, new Predicate<RequestFile>() {

            @Override
            public boolean apply(RequestFile requestFile) {
                return requestFile.getType() == RequestFile.TYPE.PAYMENT || requestFile.getType() == RequestFile.TYPE.BENEFIT;
            }
        }));
        for (final RequestFile file : suitedFiles) {
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                log.info("Binding payment file. Name: {}, directory: {}", file.getName(), file.getDirectory());

                //find associated benefit file
                RequestFile benefitFile = null;
                try {
                    benefitFile = Iterables.find(suitedFiles, new Predicate<RequestFile>() {

                        @Override
                        public boolean apply(RequestFile benefitFile) {
                            return (benefitFile.getType() == RequestFile.TYPE.BENEFIT) && benefitFile.getGroupId().equals(file.getGroupId());
                        }
                    });
                } catch (NoSuchElementException e) {
                }

                if (benefitFile != null) {
                    log.info("Binding paired benefit file. Name: {}, directory: {}", benefitFile.getName(), benefitFile.getDirectory());
                }

                bindingThreadPool.submit(new BindTask(file, benefitFile));
            }
        }
    }

    /**
     * Запускает процесс обработки файлов
     * @param requestFiles
     */
    public void process(List<RequestFile> requestFiles) {
        List<RequestFile> suitedFiles = Lists.newArrayList(Iterables.filter(requestFiles, new Predicate<RequestFile>() {

            @Override
            public boolean apply(RequestFile requestFile) {
                return (requestFile.getType() == RequestFile.TYPE.PAYMENT || requestFile.getType() == RequestFile.TYPE.BENEFIT)
                        && (requestFile.getStatus() != RequestFile.STATUS.SAVED) && (requestFile.getStatus() != RequestFile.STATUS.SAVE_ERROR);
            }
        }));
        for (final RequestFile file : suitedFiles) {
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                log.info("Processing payment file. Name: {}, direcory: {}", file.getName(), file.getDirectory());

                //find associated benefit file
                RequestFile benefitFile = null;
                try {
                    benefitFile = Iterables.find(suitedFiles, new Predicate<RequestFile>() {

                        @Override
                        public boolean apply(RequestFile benefitFile) {
                            return (benefitFile.getType() == RequestFile.TYPE.BENEFIT) && benefitFile.getGroupId().equals(file.getGroupId());

                        }
                    });
                } catch (NoSuchElementException e) {
                }

                if (benefitFile != null) {
                    log.info("Processing paired benefit file. Name: {}, directory: {}", benefitFile.getName(), benefitFile.getDirectory());
                }
                processingThreadPool.submit(new ProcessTask(file, benefitFile));
            }
        }
    }
}
