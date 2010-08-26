package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.storage.RequestFileStorage;

import javax.ejb.Stateless;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:15:53
 */
@Stateless(name = "FileBean")
public class RequestFileBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();

    public final static String REQUEST_PAYMENT_FILES_PREFIX = "A_";

    public final static String REQUEST_BENEFIT_FILES_PREFIX = "AF";

    public final static String REQUEST_FILES_POSTFIX = ".dbf";

    private List<File> getRequestFiles(final String filePrefix, final String districtDir,
            final String[] osznCode, final String[] months) {

        return RequestFileStorage.getInstance().getFiles(districtDir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (dir.getName().equalsIgnoreCase(districtDir)) {
                    for (String oszn : osznCode) {
                        for (String month : months) {
                            if (name.equalsIgnoreCase(filePrefix + oszn + month + REQUEST_FILES_POSTFIX)) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        });
    }

    public List<File> getRequestPaymentFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(REQUEST_PAYMENT_FILES_PREFIX, districtDir, osznCode, months);
    }

    public List<File> getRequestBenefitFiles(String districtDir, String[] osznCode, String[] months) {
        return getRequestFiles(REQUEST_BENEFIT_FILES_PREFIX, districtDir, osznCode, months);
    }

    private String[] getMonth(int monthFrom, int monthTo) {
        String[] months = new String[monthTo - monthFrom + 1];

        int index = 0;
        for (int m = monthFrom; m <= monthTo; ++m, ++index) {
            months[index] = (m < 10 ? "0" + m : "") + m;
        }

        return months;
    }

    public void load(int monthFrom, int monthTo) {
        List<File> requestPaymentFiles = getRequestPaymentFiles("LE", new String[]{"1760"}, getMonth(monthFrom, monthTo));
        List<File> requestBenefitFiles = getRequestBenefitFiles("LE", new String[]{"1760"}, getMonth(monthFrom, monthTo));


    }

    public void save(RequestFile requestFile) {
        if (requestFile.getId() == null) {
            sqlSession.insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);
        } else {
            sqlSession.update(MAPPING_NAMESPACE + ".updateRequestFile", requestFile);
        }
    }

    public RequestFile findById(long fileId) {
        return (RequestFile) sqlSession.selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }
}
