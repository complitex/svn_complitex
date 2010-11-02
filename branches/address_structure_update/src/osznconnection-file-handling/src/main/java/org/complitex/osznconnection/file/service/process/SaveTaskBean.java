package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionaryfw.service.executor.ExecuteException;
import org.complitex.dictionaryfw.service.executor.ITaskBean;
import org.complitex.osznconnection.file.entity.RequestFileGroup;

import javax.ejb.Stateless;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:58
 */
@Stateless(name = "SaveTaskBean")
public class SaveTaskBean implements ITaskBean<RequestFileGroup>{

    @Override
    public boolean execute(RequestFileGroup object) throws ExecuteException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onError(RequestFileGroup object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getModuleName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
