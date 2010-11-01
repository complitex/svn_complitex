package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:57
 */
public class LoadUtil {
    public static class LoadParameter {
        List<RequestFileGroup> requestFileGroups;
        List<RequestFile> linkError;

        public LoadParameter(List<RequestFileGroup> requestFileGroups, List<RequestFile> linkError) {
            this.requestFileGroups = requestFileGroups;
            this.linkError = linkError;
        }

        public List<RequestFileGroup> getRequestFileGroups() {
            return requestFileGroups;
        }

        public List<RequestFile> getLinkError() {
            return linkError;
        }
    }

    private static List<File> getFiles(final String districtDir, final int monthFrom, final int monthTo)
            throws StorageNotFoundException {

        return RequestFileStorage.getInstance().getInputFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                if(file.isDirectory()){
                    return true;
                }

                String name = file.getName();

                //TARIF
                if (name.equalsIgnoreCase("TARIF12.DBF")) {
                    return true;
                }else{ //PAYMENT, BENEFIT
                    for (int m = monthFrom; m <= monthTo; ++m) {
                        String month = (m <= 9 ? "0" + (m + 1) : "" + (m + 1));
                        String pattern = "((A_)|(AF))\\d{4}" + month + "\\.DBF";

                        if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(name).matches()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    private static String getPrefix(String name){
        return name.length() > 11 ? name.substring(0,2) : "";
    }

    private static String getSuffix(String name){
        return name.length() > 11 ? name.substring(2,8) : "";
    }

    private static RequestFile newRequestFile(File file, Long organizationId, int year){
        RequestFile requestFile = new RequestFile();

        requestFile.setName(file.getName());
        requestFile.updateTypeByName();
        requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file));
        requestFile.setLength(file.length());
        requestFile.setAbsolutePath(file.getAbsolutePath());
        requestFile.setOrganizationId(organizationId);
        requestFile.setMonth(Integer.parseInt(file.getName().substring(6, 8)));
        requestFile.setYear(year);
        return requestFile;
    }

    public static LoadParameter getLoadParameter(Long organizationId, String districtCode, int monthFrom, int monthTo, int year)
            throws StorageNotFoundException {
        List<File> files = getFiles(districtCode, monthFrom, monthTo);

        Map<String, Map<String, RequestFileGroup>> requestFileGroupsMap = new HashMap<String, Map<String, RequestFileGroup>>();

        //payment
        for (int i=0; i < files.size(); ++i){
            File file = files.get(i);

            if (RequestFile.PAYMENT_FILE_PREFIX.equals(getPrefix(file.getName()))){
                RequestFileGroup group = new RequestFileGroup();

                group.setPaymentFile(newRequestFile(file, organizationId, year));

                files.remove(i);
                i--;

                Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                if (map == null){
                    map = new HashMap<String, RequestFileGroup>();
                    requestFileGroupsMap.put(file.getParent(), map);
                }

                map.put(getSuffix(file.getName()), group);
            }
        }

        List<RequestFile> linkError = new ArrayList<RequestFile>();

        //benefit
        for (File file : files){
            if (RequestFile.BENEFIT_FILE_PREFIX.equals(getPrefix(file.getName()))){
                RequestFile requestFile = newRequestFile(file, organizationId, year);

                Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                if (map != null){
                    RequestFileGroup group = map.get(getSuffix(file.getName()));

                    if (group != null){
                        group.setBenefitFile(requestFile);
                        continue;
                    }
                }

                requestFile.setStatus(RequestFile.STATUS.LOAD_ERROR);
                requestFile.setStatusDetail(RequestFile.STATUS_DETAIL.LINKED_FILE_NOT_FOUND);

                linkError.add(requestFile);
            }
        }

        List<RequestFileGroup> requestFileGroups = new ArrayList<RequestFileGroup>();

        for (Map<String, RequestFileGroup> map : requestFileGroupsMap.values()){
            for (RequestFileGroup group : map.values()){
                if (group.getBenefitFile() != null){
                    requestFileGroups.add(group);
                }else{
                    RequestFile payment = group.getPaymentFile();

                    payment.setStatus(RequestFile.STATUS.LOAD_ERROR);
                    payment.setStatusDetail(RequestFile.STATUS_DETAIL.LINKED_FILE_NOT_FOUND);

                    linkError.add(payment);
                }
            }
        }

        return new LoadParameter(requestFileGroups, linkError);
    }
}
