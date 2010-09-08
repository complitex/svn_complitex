package org.complitex.osznconnection.file.service;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;
import org.complitex.dictionaryfw.entity.Log;
import org.complitex.dictionaryfw.service.LogBean;
import org.complitex.dictionaryfw.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.exception.AlreadyLoadedException;
import org.complitex.osznconnection.file.service.exception.FieldNotFoundException;
import org.complitex.osznconnection.file.service.exception.FieldWrongTypeException;
import org.complitex.osznconnection.file.service.exception.SqlSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.concurrent.Future;

import static org.complitex.osznconnection.file.entity.RequestFile.STATUS.*;
import static org.complitex.osznconnection.file.entity.RequestFile.STATUS_DETAIL.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.09.2010 15:36:11
 */
@Stateless(name = "LoadTaskBean")
@SuppressWarnings({"EjbProhibitedPackageUsageInspection"})
public class LoadTaskBean {
    private static final Logger log = LoggerFactory.getLogger(LoadTaskBean.class);

    @EJB(beanName = "PaymentBean")
    private PaymentBean paymentBean;

    @EJB(beanName = "BenefitBean")
    private BenefitBean benefitBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;          

    @Asynchronous
    public Future<RequestFile> load(File file, long organizationId,  int year){
        RequestFile requestFile =  new RequestFile();
        requestFile.setLength(file.length());
        requestFile.setName(file.getName());
        requestFile.setDate(parseDate(file.getName(), year));

        try {
            //проверка загружен ли файл
            if (requestFileBean.isLoaded(requestFile)){
                throw new AlreadyLoadedException();
            }

            DBFReader dbfReader = new DBFReader(new FileInputStream(file));
            dbfReader.setCharactersetName("cp866");
            requestFile.setDbfRecordCount(dbfReader.getRecordCount());

            //Начало загрузки
            requestFile.setLoaded(DateUtil.getCurrentDate());
            requestFile.setOrganizationObjectId(organizationId);
            requestFile.setStatus(LOADING);

            requestFileBean.save(requestFile);

            //Загрузка записей
            if (requestFile.isPayment()){
                paymentBean.load(requestFile, dbfReader);
            }else if (requestFile.isBenefit()){
                benefitBean.load(requestFile, dbfReader);
            }

            //Загрузка завершена
            requestFile.setStatus(LOADED);
        }catch (FieldNotFoundException e){
            requestFile.setStatus(LOAD_ERROR, FIELD_NOT_FOUND);
            log.error("Поле не найдено " + file.getAbsolutePath(), e);
            error(requestFile, "Поле {0} не найдено в файле {1}", e.getMessage(), file.getName());
        } catch (FieldWrongTypeException e) {
            requestFile.setStatus(LOAD_ERROR, FIELD_WRONG_TYPE);
            log.error("Неверный тип поля " + file.getAbsolutePath(), e);
            error(requestFile, "Неверный тип поля {0} в файле {1}", e.getMessage(), file.getName());
        } catch (AlreadyLoadedException e) {
            requestFile.setStatus(LOAD_ERROR, ALREADY_LOADED);
            log.warn("Файл уже загружен {}", file.getAbsolutePath());
            error(requestFile, "Файл уже загружен {0}", file.getName());
        } catch (SqlSessionException e){
            requestFile.setStatus(LOAD_ERROR, SQL_SESSION);
            log.error("Ошибка сохранения в базу данных при обработке файла " + file.getAbsolutePath(), e);
            error(requestFile, "Ошибка сохранения в базу данных файла {0}. {1}", file.getName(), e.getMessage());
        } catch (DBFException e){
            requestFile.setStatus(LOAD_ERROR, DBF);
            log.error("Ошибка формата файла " + file.getAbsolutePath(), e);
            error(requestFile, "Ошибка формата файла {0} {1}", file.getName(), e.getMessage());
        } catch (Throwable t){
            requestFile.setStatus(LOAD_ERROR, CRITICAL);
            log.error("Ошибка загрузки файла " + file.getAbsolutePath(), t);
            error(requestFile, "Ошибка загрузки файла {0}", file.getName());
        } finally {
            if (requestFile.getStatusDetail() != ALREADY_LOADED) {
                requestFile.setLoaded(DateUtil.getCurrentDate());
                requestFileBean.save(requestFile);

                if (requestFile.getStatus() == LOADED){
                    log.info("Файл успешно загружен {}", file.getName());
                    info(requestFile, "Файл успешно загружен {0}", file.getName());
                }
            }
        }

        return new AsyncResult<RequestFile>(requestFile);
    }

    private Date parseDate(String name, int year){
        return DateUtil.parseDate(name.substring(6,8), year);
    }

    private void info(RequestFile requestFile, String decs, Object... args){
        logBean.info(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }

    private void error(RequestFile requestFile, String decs, Object... args){
        logBean.error(
                Module.NAME,
                LoadRequestBean.class,
                RequestFile.class,
                null,
                requestFile.getId(),
                Log.EVENT.CREATE,
                requestFileBean.getLogChangeList(requestFile),
                decs,
                args);
    }
}
