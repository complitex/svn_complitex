package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.executor.ExecuteException;
import org.complitex.osznconnection.file.service.exception.AbstractSkippedException;
import org.complitex.osznconnection.file.service.executor.ITaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 16:08:40
 */
public abstract class AbstractTaskBean{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EJB(beanName = "LogBean")
    protected LogBean logBean;

    @EJB(beanName = "PaymentBean")
    protected PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    protected BenefitBean benefitBean;

    @EJB(beanName = "TarifBean")
    protected TarifBean tarifBean;

    @EJB(beanName = "RequestFileBean")
    protected RequestFileBean requestFileBean;

    @EJB(beanName = "RequestFileGroupBean")
    protected RequestFileGroupBean requestFileGroupBean;

    @EJB(beanName = "ConfigBean")
    protected ConfigBean configBean;

    @Asynchronous
    public void asyncExecute(RequestFileGroup group, ITaskListener listener) {
//        try {
//            execute(group);
//            listener.complete(group);
//        } catch (ExecuteException e){
//            listener.error(group, e); //handled exception
//        } catch (Exception e){
//            listener.error(group, e); //critical exception
//        }
    }

    protected abstract void execute(RequestFileGroup group) throws ExecuteException, AbstractSkippedException;

    /**
     * Обновляет статус файла в базе данных, логирует в лог и в журнал событий, выбрасывает исключение
     * @param e Ошибка выполнения задачи
     * @param status Статус
     * @param detail Описание статуса
     * @throws org.complitex.osznconnection.file.service.executor.ExecuteException Ошибка выполнения задачи
     */
    protected void executionError(ExecuteException e, RequestFile.STATUS status, RequestFile.STATUS_DETAIL detail)
            throws ExecuteException {
//        requestFileBean.updateStatus(e.getRequestFile(), status, detail);

        log.error(e.getMessage(), e);
//        error(e.getRequestFile(), e.getMessage());

        throw e;
    }

    /**
     * Обновляет статус файла, логирует в лог и в журнал событий, выбрасывает исключение
     * @param e Выполнение задачи пропущено
     * @param status Статус
     * @param detail Описание статуса
     * @throws AbstractSkippedException Задача пропущена
     */
    protected void executionSkip(AbstractSkippedException e, RequestFile.STATUS status, RequestFile.STATUS_DETAIL detail)
            throws AbstractSkippedException {
        e.getRequestFile().setStatus(status, detail);

        log.warn(e.getMessage());
        info(e.getRequestFile(), e.getMessage());

        throw e;
    }

    protected void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(Module.NAME, getClass(), RequestFile.class, null, requestFile.getId(), Log.EVENT.EDIT,
                requestFile.getLogChangeList(), decs, args);
    }

    protected void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(Module.NAME, getClass(), RequestFile.class, null, requestFile.getId(), Log.EVENT.EDIT,
                requestFile.getLogChangeList(), decs, args);
    }
}
