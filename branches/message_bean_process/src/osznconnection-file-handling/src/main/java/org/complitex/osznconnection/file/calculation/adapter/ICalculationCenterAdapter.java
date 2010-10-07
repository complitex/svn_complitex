/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.util.Date;
import java.util.List;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.Payment;

/**
 *
 * @author Artem
 */
public interface ICalculationCenterAdapter {

    void prepareCity(Payment payment, String city, String cityCode);

    void prepareDistrict(Payment payment, String district, String districtCode);

    void prepareStreet(Payment payment, String street, String streetCode);

    void prepareStreetType(Payment payment, String streetType, String streetTypeCode);

    void prepareBuilding(Payment payment, String buildingNumber, String buildingCorp, String buildingCode);

    void prepareApartment(Payment payment, String apartment, String apartmentCode);

    void acquirePersonAccount(Payment payment);

    public List<AccountDetail> acquireAccountCorrectionDetails(Payment payment);

    public void processPaymentAndBenefit(Payment payment, Benefit benefit, long calculationCenterId);

    public void processBenefit(Date dat1, List<Benefit> benefits, long calculationCenterId);
}
