package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.dictionaryfw.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.BenefitBean;
import org.complitex.osznconnection.file.service.PaymentBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:57
 */
@Stateless(name = "LoadGroupTaskBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadGroupTaskBean implements ITaskBean<RequestFileGroup>{
    private static final Logger log = LoggerFactory.getLogger(LoadGroupTaskBean.class);

    @EJB(beanName = "PaymentBean")
    protected PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    protected BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    protected RequestFileBean requestFileBean;

    @EJB(beanName = "RequestFileGroupBean")
    private RequestFileGroupBean requestFileGroupBean;

    @EJB(beanName = "LoadRequestFileBean")
    private LoadRequestFileBean loadRequestFileBean;

    @Override
    public boolean execute(RequestFileGroup group) throws ExecuteException {
        group.setStatus(RequestFileGroup.STATUS.LOADING);
        requestFileGroupBean.save(group);

        group.getBenefitFile().setGroupId(group.getId());
        group.getPaymentFile().setGroupId(group.getId());

        //load payment
        boolean noSkip = loadRequestFileBean.load(group.getPaymentFile(), new LoadRequestFileBean.ILoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return PaymentDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new Payment();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                paymentBean.insert(batch);
            }
        });

        // load benefit
        if (noSkip){
            loadRequestFileBean.load(group.getBenefitFile(), new LoadRequestFileBean.ILoadRequestFile() {

                @Override
                public Enum[] getFieldNames() {
                    return BenefitDBF.values();
                }

                @Override
                public AbstractRequest newObject() {
                    return new Benefit();
                }

                @Override
                public void save(List<AbstractRequest> batch) {
                    benefitBean.insert(batch);
                }
            });
        } else {
            requestFileGroupBean.clearEmptyGroup();
            return false; //skip - file already loaded
        }

        group.setStatus(RequestFileGroup.STATUS.LOADED);
        requestFileGroupBean.save(group);

        return true;
    }

    @Override
    public void onError(RequestFileGroup object) {

        requestFileGroupBean.clearEmptyGroup();
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return LoadGroupTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
