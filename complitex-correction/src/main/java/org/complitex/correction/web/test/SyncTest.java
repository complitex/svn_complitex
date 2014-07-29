package org.complitex.correction.web.test;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.complitex.address.entity.AddressSync;
import org.complitex.address.service.AddressSyncAdapter;
import org.complitex.dictionary.entity.Cursor;
import org.complitex.dictionary.util.StringUtil;

import javax.ejb.EJB;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.13 16:17
 */
public class SyncTest extends WebPage {
    @EJB
    private AddressSyncAdapter addressSyncService;

    public SyncTest() {
        final MultiLineLabel districts = new MultiLineLabel("districts");
        add(districts);

        final MultiLineLabel streetTypes = new MultiLineLabel("streetTypes");
        add(streetTypes);

        final MultiLineLabel streets = new MultiLineLabel("streets");
        add(streets);

        final MultiLineLabel buildings = new MultiLineLabel("buildings");
        add(buildings);

        Form form = new Form("form");
        add(form);

        form.add(new Button("test"){
            @Override
            public void onSubmit() {
                String dataSource = "jdbc/osznconnectionRemoteResource";

                //districts
                Cursor<AddressSync> districtSyncs = addressSyncService.getDistrictSyncs(
                        dataSource,
                        "Тверь", "г", new Date());
                if (districtSyncs != null) {
                    String t = "";

                    for (AddressSync d : districtSyncs.getList()){
                        t += d.getExternalId() + " " + d.getName();
                    }

                    districts.setDefaultModel(new Model<>(t));
                }

                //street types
                Cursor<AddressSync> streetTypeSyncs = addressSyncService.getStreetTypeSyncs(dataSource);
                if (streetTypeSyncs != null) {
                    String t = "";

                    for (AddressSync s : streetTypeSyncs.getList()){
                        t += s.getExternalId() + " " +s.getAdditionalName() + " " + s.getName() + "\n";
                    }

                    streetTypes.setDefaultModel(new Model<>(t));
                }

                //streets
                Cursor<AddressSync> streetSyncs = addressSyncService.getStreetSyncs(dataSource,
                        "Тверь", "г", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (AddressSync s : streetSyncs.getList()){
                        t += s.getExternalId() + " " + s.getAdditionalName() + " " + s.getName() + "\n";
                    }

                    streets.setDefaultModel(new Model<>(t));
                }

                //buildings
                Cursor<AddressSync> buildingAddressSyncs = addressSyncService.getBuildingSyncs(dataSource,
                        "Центральный", "ул", "ФРАНТИШЕКА КРАЛА", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (AddressSync s : buildingAddressSyncs.getList()){
                        t +=  s.getExternalId() + " " + s.getName() + " "
                                + StringUtil.emptyOnNull(s.getAdditionalName())+"\n";
                    }

                    buildings.setDefaultModel(new Model<>(t));
                }

            }
        });
    }
}
