/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Artem
 */
public class FileExecutorService {

    private static final Logger log = LoggerFactory.getLogger(FileExecutorService.class);

    private static FileExecutorService fileExecutorService;

    private FileExecutorService() {
    }

    public synchronized static FileExecutorService get() {
        if (fileExecutorService == null) {
            fileExecutorService = new FileExecutorService();
        }
        return fileExecutorService;
    }

    private ExecutorService threadPool = initThreadPool();

    private static ExecutorService initThreadPool() {
        return Executors.newFixedThreadPool(FileHandlingConfig.BINDING_THREAD_SIZE.getInteger());
    }

    private int taskCount;

    public synchronized boolean isBinding() {
        return taskCount > 0;
    }

    private synchronized void incrementTaskCount() {
        taskCount++;
    }

    private synchronized void decrementTaskCount() {
        taskCount--;
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
            incrementTaskCount();
            try {
                //TODO: remove it after test
                Thread.sleep(5000);
                
                BindingRequestBean bindingRequestBean = getBindingBean();
                bindingRequestBean.bindPaymentAndBenefit(paymentFile, benefitFile);
            } catch (Exception e) {
                log.error("", e);
            } finally {
                decrementTaskCount();
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

    public void bind(List<RequestFile> requestFiles) {
        List<RequestFile> suitedFiles = Lists.newArrayList(Iterables.filter(requestFiles, new Predicate<RequestFile>() {

            @Override
            public boolean apply(RequestFile requestFile) {
                return requestFile.getStatus().equals(RequestFile.STATUS.LOADED);
            }
        }));
        Set<Long> bindingBenefitFiles = Sets.newHashSet();
        for (final RequestFile file : suitedFiles) {
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                log.info("Payment file : {}", file.getName());

                //find associated benefit file
                RequestFile benefitFile = null;
                try {
                    benefitFile = Iterables.find(requestFiles, new Predicate<RequestFile>() {

                        @Override
                        public boolean apply(RequestFile benefitFile) {
                            return benefitFile.getType() == RequestFile.TYPE.BENEFIT
                                    && benefitFile.getName().substring(RequestFile.PAYMENT_FILES_PREFIX.length()).
                                    equalsIgnoreCase(file.getName().substring(RequestFile.BENEFIT_FILES_PREFIX.length()));

                        }
                    });
                } catch (NoSuchElementException e) {
                }

                if (benefitFile != null) {
                    bindingBenefitFiles.add(benefitFile.getId());
                    log.info("Benefit file : {}", benefitFile.getName());
                }

                threadPool.submit(new BindTask(file, benefitFile));
//                new BindTask(file, paymentStatus, benefitFile, benefitStatus).run();
//               new Thread(new BindTask(file, paymentStatus, benefitFile, benefitStatus));
            }
        }
        for (RequestFile file : suitedFiles) {
            if ((file.getType() == RequestFile.TYPE.BENEFIT) && !bindingBenefitFiles.contains(file.getId())) {
                threadPool.submit(new BindTask(null, file));
            }
        }
    }
}
