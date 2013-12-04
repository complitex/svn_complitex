package org.complitex.address.service;

import org.complitex.address.Module;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.dictionary.entity.DictionaryConfig;
import org.complitex.dictionary.entity.IImportFile;
import org.complitex.dictionary.entity.ImportMessage;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.exception.*;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.organization.entity.OrganizationImportFile;
import org.complitex.organization.service.OrganizationImportService;
import org.complitex.organization.service.exception.RootOrganizationNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.02.11 18:05
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class ImportService {

    private final static Logger log = LoggerFactory.getLogger(ImportService.class);
    public static final long INT_ORG_ID = 0L;

    @Resource
    private UserTransaction userTransaction;

    @EJB
    private AddressImportService addressImportService;

    @EJB
    private OrganizationImportService organizationImportService;

    @EJB
    private ConfigBean configBean;

    @EJB
    private LogBean logBean;

    private boolean processing;
    private boolean error;
    private boolean success;
    private String errorMessage;
    private Map<IImportFile, ImportMessage> dictionaryMap = new LinkedHashMap<>();
    private IImportListener dictionaryListener = new IImportListener() {

        @Override
        public void beginImport(IImportFile importFile, int recordCount) {
            dictionaryMap.put(importFile, new ImportMessage(importFile, recordCount, 0));
        }

        @Override
        public void recordProcessed(IImportFile importFile, int recordIndex) {
            dictionaryMap.get(importFile).setIndex(recordIndex);
        }

        @Override
        public void completeImport(IImportFile importFile, int recordCount) {
            logBean.info(Module.NAME, ImportService.class, importFile.getClass(), null, Log.EVENT.CREATE,
                    "Имя файла: {0}, количество записей: {1}", importFile.getFileName(), recordCount);
        }

        @Override
        public void warn(IImportFile importFile, String message) {
        }
    };

    public boolean isProcessing() {
        return processing;
    }

    public boolean isError() {
        return error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ImportMessage getMessage(IImportFile importFile) {
        return dictionaryMap.get(importFile);
    }

    private void init() {
        dictionaryMap.clear();
        processing = true;
        error = false;
        success = false;
        errorMessage = null;
    }

    private <T extends IImportFile> void processDictionary(T importFile, long localeId) throws ImportFileNotFoundException,
            ImportObjectLinkException, ImportFileReadException, ImportDuplicateException, RootOrganizationNotFound {
        if (importFile instanceof AddressImportFile) { //Address
            addressImportService.process(importFile, dictionaryListener, localeId, DateUtil.getCurrentDate());
        } else if (importFile instanceof OrganizationImportFile){ //Organization
            organizationImportService.process(dictionaryListener, localeId, DateUtil.getCurrentDate());
        }
    }

    @Asynchronous
    public <T extends IImportFile> void process(List<T> dictionaryFiles, long localeId) {
        if (processing) {
            return;
        }

        init();

        configBean.getString(DictionaryConfig.IMPORT_FILE_STORAGE_DIR, true); //reload config cache

        try {
            //Dictionary
            for (T t : dictionaryFiles) {
                userTransaction.begin();

                processDictionary(t, localeId);

                userTransaction.commit();
            }

            success = true;
        } catch (Exception e) {
            log.error("Ошибка импорта", e);

            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("Ошибка отката транзакции", e1);
            }

            error = true;
            errorMessage = e instanceof AbstractException ? e.getMessage() : new ImportCriticalException(e).getMessage();

            logBean.error(Module.NAME, ImportService.class, null, null, Log.EVENT.CREATE, errorMessage);
        } finally {
            processing = false;
        }
    }
}
