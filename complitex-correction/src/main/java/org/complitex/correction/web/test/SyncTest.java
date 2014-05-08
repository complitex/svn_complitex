package org.complitex.correction.web.test;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.complitex.address.entity.BuildingAddressSync;
import org.complitex.address.entity.DistrictSync;
import org.complitex.address.entity.StreetSync;
import org.complitex.address.entity.StreetTypeSync;
import org.complitex.address.service.AddressSyncAdapter;
import org.complitex.dictionary.util.StringUtil;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

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
                String dataSource = "jdbc/osznconnection_remote_resource";

                //districts
                List<DistrictSync> districtSyncs = addressSyncService.getDistrictSyncs(
                        dataSource,
                        "Тверь", "г", new Date());
                if (districtSyncs != null) {
                    String t = "";

                    for (DistrictSync d : districtSyncs){
                        t += d.getExternalId() + " " + d.getName();
                    }

                    districts.setDefaultModel(new Model<>(t));
                }

                //street types
                List<StreetTypeSync> streetTypeSyncs = addressSyncService.getStreetTypeSyncs(dataSource);
                if (streetTypeSyncs != null) {
                    String t = "";

                    for (StreetTypeSync s : streetTypeSyncs){
                        t += s.getExternalId() + " " +s.getShortName() + " " + s.getName() + "\n";
                    }

                    streetTypes.setDefaultModel(new Model<>(t));
                }

                //streets
                List<StreetSync> streetSyncs = addressSyncService.getStreetSyncs(dataSource,
                        "Тверь", "г", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (StreetSync s : streetSyncs){
                        t += s.getExternalId() + " " + s.getStreetTypeShortName() + " " + s.getName() + "\n";
                    }

                    streets.setDefaultModel(new Model<>(t));
                }

                //buildings
                List<BuildingAddressSync> buildingAddressSyncs = addressSyncService.getBuildingSyncs(dataSource,
                        "Центральный", "ул", "ФРАНТИШЕКА КРАЛА", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (BuildingAddressSync s : buildingAddressSyncs){
                        t +=  s.getStreetExternalId() + " " + s.getExternalId() + " " + s.getName() + " "
                                + StringUtil.emptyOnNull(s.getPart())+"\n";
                    }

                    buildings.setDefaultModel(new Model<>(t));
                }

            }
        });
    }
}
