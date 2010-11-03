package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.dictionaryfw.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Tarif;
import org.complitex.osznconnection.file.entity.TarifDBF;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.TarifBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.11.10 13:03
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class LoadTarifTaskBean implements ITaskBean<RequestFile>{
    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LoadRequestFileBean")
    private LoadRequestFileBean loadRequestFileBean;

    @EJB(beanName = "TarifBean")
    private TarifBean tarifBean;

    @Override
    public boolean execute(RequestFile requestFile) throws ExecuteException {
        //delete previous tarif
        requestFileBean.deleteTarif(requestFile.getOrganizationId());

        loadRequestFileBean.load(requestFile, new LoadRequestFileBean.ILoadRequestFile(){

            @Override
            public Enum[] getFieldNames() {
                return TarifDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new Tarif();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                tarifBean.insert(batch);
            }
        });

        return true;
    }

    @Override
    public void onError(RequestFile requestFile) {
        requestFileBean.delete(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return LoadTarifTaskBean.class;
    }
}
