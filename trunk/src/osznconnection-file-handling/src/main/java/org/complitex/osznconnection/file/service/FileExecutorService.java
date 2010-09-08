/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    public boolean isBinding() {
        return !bindingFilesMap.isEmpty();
    }

    private ConcurrentHashMap<Long, RequestFile> bindingFilesMap = new ConcurrentHashMap<Long, RequestFile>();

    public Collection<RequestFile> getBindingFiles() {
        return Collections.unmodifiableCollection(bindingFilesMap.values());
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
                BindingRequestBean bindingRequestBean = getBindingBean();

                if (paymentFile != null) {
                    try {
                        bindingFilesMap.putIfAbsent(paymentFile.getId(), paymentFile);
                        bindingRequestBean.bindPaymentFile(paymentFile);
                    } catch (Exception e) {
                        log.error("", e);
                    } finally {
                        bindingFilesMap.remove(paymentFile.getId());
                    }
                }

                if (benefitFile != null) {
                    bindingFilesMap.putIfAbsent(benefitFile.getId(), benefitFile);
                    try {
                        bindingRequestBean.bindBenefitFile(benefitFile);
                    } catch (Exception e) {
                        log.error("", e);
                    } finally {
                        bindingFilesMap.remove(benefitFile.getId());
                    }
                }
            } catch (Exception e) {
                log.error("", e);
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
                return requestFile.getStatus() == RequestFile.STATUS.LOADED || requestFile.getStatus() == RequestFile.STATUS.BOUND_WITH_ERRORS;
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
                    log.info("Paired benefit file : {}", benefitFile.getName());
                }

                threadPool.submit(new BindTask(file, benefitFile));
//                new BindTask(file, paymentStatus, benefitFile, benefitStatus).run();
//               new Thread(new BindTask(file, paymentStatus, benefitFile, benefitStatus));
            }
        }
        for (RequestFile file : suitedFiles) {
            if ((file.getType() == RequestFile.TYPE.BENEFIT) && !bindingBenefitFiles.contains(file.getId())) {
                log.info("Single benefit file : {}", file.getName());
                threadPool.submit(new BindTask(null, file));
            }
        }
    }
}
