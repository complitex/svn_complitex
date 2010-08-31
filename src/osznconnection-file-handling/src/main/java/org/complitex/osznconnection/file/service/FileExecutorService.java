/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.complitex.osznconnection.file.entity.AsyncOperationStatus;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return Executors.newFixedThreadPool(10);
    }

    private Map<Long, AsyncOperationStatus> fileToProcessStatusMap = new ConcurrentHashMap<Long, AsyncOperationStatus>();

    private class BindTask implements Runnable {

        private RequestFile paymentFile;

        private AsyncOperationStatus paymentStatus;

        private RequestFile benefitFile;

        private AsyncOperationStatus benefitStatus;

        public BindTask(RequestFile paymentFile, AsyncOperationStatus paymentStatus, RequestFile benefitFile, AsyncOperationStatus benefitStatus) {
            this.paymentFile = paymentFile;
            this.paymentStatus = paymentStatus;
            this.benefitFile = benefitFile;
            this.benefitStatus = benefitStatus;
        }

        @Override
        public void run() {
            BindingRequestBean bindingRequestBean = getBindingBean();
            bindingRequestBean.bindPaymentAndBenefit(paymentFile, paymentStatus, benefitFile, benefitStatus);
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

    public Map<Long, AsyncOperationStatus> getFileToProcessStatusMap() {
        return fileToProcessStatusMap;
    }

    public void bind(List<RequestFile> requestFiles) {
        for (final RequestFile file : requestFiles) {
            log.info("File : {}", file.getName());
            if (file.getType() == RequestFile.TYPE.PAYMENT) {
                AsyncOperationStatus paymentStatus = new AsyncOperationStatus(file);
                fileToProcessStatusMap.put(file.getId(), paymentStatus);

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

                AsyncOperationStatus benefitStatus = null;
                if (benefitFile != null) {
                    benefitStatus = new AsyncOperationStatus(benefitFile);
                    fileToProcessStatusMap.put(benefitFile.getId(), benefitStatus);
                }

//                threadPool.submit(new BindTask(file, paymentStatus, benefitFile, benefitStatus));
//                new BindTask(file, paymentStatus, benefitFile, benefitStatus).run();
               new Thread(new BindTask(file, paymentStatus, benefitFile, benefitStatus));
            }
        }
    }
}
