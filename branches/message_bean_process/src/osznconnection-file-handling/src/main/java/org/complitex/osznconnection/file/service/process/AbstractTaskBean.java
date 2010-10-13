package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.ExecuteException;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.2010 16:08:40
 */
public abstract class AbstractTaskBean{
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
        try {
            execute(group);
            listener.complete(group);
        } catch (ExecuteException e){
            listener.error(group, e); //handled exception
        } catch (Exception e){
            listener.error(group, e); //critical exception
        }
    }

    protected abstract void execute(RequestFileGroup group) throws ExecuteException;

    protected void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(
                Module.NAME,
                getClass(),
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFile.getLogChangeList(),
                decs,
                args);
    }

    protected void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(
                Module.NAME,
                getClass(),
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFile.getLogChangeList(),
                decs,
                args);
    }
}
