/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Artem
 */
@Deprecated
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
        return Executors.newFixedThreadPool(ConfigStatic.get().getInteger(Config.BIND_THREADS_SIZE, true));
    }

    public boolean isBinding() {
        return bindingCounter.get() > 0;
    }

    private ExecutorService processingThreadPool = initProcessingThreadPool();

    private static ExecutorService initProcessingThreadPool() {
        return Executors.newFixedThreadPool(ConfigStatic.get().getInteger(Config.FILL_THREADS_SIZE, true));
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

                if (paymentFile != null) {
                    inBinding.add(paymentFile);

                    try {
                        bindingRequestBean.bindPaymentFile(paymentFile);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                if (benefitFile != null) {
                    inBinding.add(benefitFile);

                    try {
                        bindingRequestBean.bindBenefitFile(benefitFile);
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
                if (paymentFile != null) {
                    inProcessing.add(paymentFile);
                    try {
                        processRequestBean.processPayment(paymentFile);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                if (benefitFile != null) {
                    inProcessing.add(benefitFile);
                    try {
                        processRequestBean.processBenefit(benefitFile);
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

    public void bind(List<RequestFile> requestFiles) {
        List<RequestFile> suitedFiles = Lists.newArrayList(Iterables.filter(requestFiles, new Predicate<RequestFile>() {

            @Override
            public boolean apply(RequestFile requestFile) {
                return (requestFile.getType() == RequestFile.TYPE.PAYMENT || requestFile.getType() == RequestFile.TYPE.BENEFIT)
                        && (requestFile.getStatus() == RequestFile.STATUS.LOADED || requestFile.getStatus() == RequestFile.STATUS.BOUND_WITH_ERRORS
                        || requestFile.getStatus() == RequestFile.STATUS.BINDED);
            }
        }));
        Set<Long> bindingBenefitFiles = Sets.newHashSet();
        for (final RequestFile file : suitedFiles) {
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                log.info("Binding payment file : {}", file.getName());

                //find associated benefit file
                RequestFile benefitFile = null;
                try {
                    benefitFile = Iterables.find(suitedFiles, new Predicate<RequestFile>() {

                        @Override
                        public boolean apply(RequestFile benefitFile) {
                            return benefitFile.getType() == RequestFile.TYPE.BENEFIT
                                    && benefitFile.getName().substring(RequestFile.PAYMENT_FILE_PREFIX.length()).
                                    equalsIgnoreCase(file.getName().substring(RequestFile.BENEFIT_FILE_PREFIX.length()));

                        }
                    });
                } catch (NoSuchElementException e) {
                }

                if (benefitFile != null) {
                    bindingBenefitFiles.add(benefitFile.getId());
                    log.info("Binding paired benefit file : {}", benefitFile.getName());
                }

                bindingThreadPool.submit(new BindTask(file, benefitFile));
//                new BindTask(file, paymentStatus, benefitFile, benefitStatus).run();
//               new Thread(new BindTask(file, paymentStatus, benefitFile, benefitStatus));
            }
        }
        for (RequestFile file : suitedFiles) {
            if ((file.getType() == RequestFile.TYPE.BENEFIT) && !bindingBenefitFiles.contains(file.getId())) {
                log.info("Binding alone benefit file : {}", file.getName());
                bindingThreadPool.submit(new BindTask(null, file));
            }
        }
    }

    public void process(List<RequestFile> requestFiles) {
        List<RequestFile> suitedFiles = Lists.newArrayList(Iterables.filter(requestFiles, new Predicate<RequestFile>() {

            @Override
            public boolean apply(RequestFile requestFile) {
                return (requestFile.getType() == RequestFile.TYPE.PAYMENT || requestFile.getType() == RequestFile.TYPE.BENEFIT)
                        && (requestFile.getStatus() == RequestFile.STATUS.BINDED || requestFile.getStatus() == RequestFile.STATUS.PROCESSED_WITH_ERRORS
                        || requestFile.getStatus() == RequestFile.STATUS.PROCESSED);
            }
        }));
        Set<Long> processingBenefitFiles = Sets.newHashSet();
        for (final RequestFile file : suitedFiles) {
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                log.info("Processing payment file : {}", file.getName());

                //find associated benefit file
                RequestFile benefitFile = null;
                try {
                    benefitFile = Iterables.find(suitedFiles, new Predicate<RequestFile>() {

                        @Override
                        public boolean apply(RequestFile benefitFile) {
                            return benefitFile.getType() == RequestFile.TYPE.BENEFIT
                                    && benefitFile.getName().substring(RequestFile.PAYMENT_FILE_PREFIX.length()).
                                    equalsIgnoreCase(file.getName().substring(RequestFile.BENEFIT_FILE_PREFIX.length()));

                        }
                    });
                } catch (NoSuchElementException e) {
                }

                if (benefitFile != null) {
                    processingBenefitFiles.add(benefitFile.getId());
                    log.info("Processing paired benefit file : {}", benefitFile.getName());
                }
                processingThreadPool.submit(new ProcessTask(file, benefitFile));
            }
        }
        for (RequestFile file : suitedFiles) {
            if ((file.getType() == RequestFile.TYPE.BENEFIT) && !processingBenefitFiles.contains(file.getId())) {
                log.info("Processing alone benefit file : {}", file.getName());
                processingThreadPool.submit(new ProcessTask(null, file));
            }
        }
    }
}
