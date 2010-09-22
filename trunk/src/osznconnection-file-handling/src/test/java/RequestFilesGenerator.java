import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.09.2010 13:12:18
 */
public class RequestFilesGenerator{
    public static String STORAGE = "C:\\Anatoly\\Java\\org.complitex.osznconnection\\storage\\generate";
    public static String[] MONTHS = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    public static String[] CODES = {"1760"};
    public static int COUNT = 1000;

    public static void main(String... arg) throws IOException {
        for (String m : MONTHS){
            for (String c : CODES){
                generate(STORAGE, RequestFile.TYPE.BENEFIT, RequestFile.BENEFIT_FILE_PREFIX, c, m, COUNT);
                generate(STORAGE, RequestFile.TYPE.PAYMENT, RequestFile.PAYMENT_FILE_PREFIX, c, m, COUNT);
            }
        }        
    }

    private static void generate(String dir, RequestFile.TYPE type, String prefix, String code, String month, int count) throws DBFException {
        Random random = new Random();

        DBFWriter writer = new DBFWriter(new File(dir, prefix + code + month + ".DBF"));

        DBFField[] fields = getDbfField(type);
        writer.setFields(fields);

        for (int i=0; i <  count; ++i){
            Object[] rowData = new Object[fields.length];

            for (int f=0; f < fields.length; ++f){
                DBFField field = fields[f];

                switch (field.getDataType()){
                    case DBFField.FIELD_TYPE_C:
                        rowData[f] = (field.getName() +": "+ UUID.randomUUID().toString()).substring(0, field.getFieldLength());
                        break;
                    case DBFField.FIELD_TYPE_N:
                        int r = random.nextInt((int) Math.pow(10,field.getFieldLength()));

                        if (field.getDecimalCount() == 0){
                            rowData[f] = r;
                        }else{
                            r /= 10;

                            rowData[f] = new BigDecimal(r).divide(new BigDecimal((int) Math.pow(10,field.getDecimalCount())));
                        }
                        break;
                    case DBFField.FIELD_TYPE_D:
                        long d = random.nextInt(60*60*24*360)*1000;

                        rowData[f] = new Date(new Date().getTime() - d);
                        break;
                }
            }

            writer.addRecord(rowData);
        }

        writer.write();
    }

    private static byte getDataType(Class type){
        if (type.equals(String.class)){
            return DBFField.FIELD_TYPE_C;
        }else if (type.equals(Integer.class) || type.equals(BigDecimal.class)){
            return DBFField.FIELD_TYPE_N;
        }else if (type.equals(Date.class)){
            return DBFField.FIELD_TYPE_D;
        }

        throw  new IllegalArgumentException(type.toString());
    }

    private static DBFField[] getDbfField(RequestFile.TYPE type){
        DBFField[] dbfFields;

        switch (type){
            case BENEFIT:
                BenefitDBF[] benefitDBFs = BenefitDBF.values();
                dbfFields = new DBFField[benefitDBFs.length];

                for (int i = 0; i < benefitDBFs.length; ++i){
                    BenefitDBF benefitDBF = benefitDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(benefitDBF.name());
                    dbfFields[i].setDataType(getDataType(benefitDBF.getType()));
                    if (!benefitDBF.getType().equals(Date.class)){
                        dbfFields[i].setFieldLength(benefitDBF.getLength());
                        dbfFields[i].setDecimalCount(benefitDBF.getScale());
                    }
                }

                return dbfFields;
            case PAYMENT:
                PaymentDBF[] paymentDBFs = PaymentDBF.values();
                dbfFields = new DBFField[paymentDBFs.length];

                for (int i = 0; i < paymentDBFs.length; ++i){
                    PaymentDBF paymentDBF = paymentDBFs[i];

                    dbfFields[i] = new DBFField();
                    dbfFields[i].setName(paymentDBF.name());
                    dbfFields[i].setDataType(getDataType(paymentDBF.getType()));
                    if (!paymentDBF.getType().equals(Date.class)) {
                        dbfFields[i].setFieldLength(paymentDBF.getLength());
                        dbfFields[i].setDecimalCount(paymentDBF.getScale());
                    }
                }

                return dbfFields;
            default: throw new IllegalArgumentException(type.name());
        }
    }

    public static void a2( String path)
            throws DBFException, IOException {

        // let us create field definitions first
        // we will go for 3 fields
        //
        DBFField fields[] = new DBFField[ 3];

        fields[0] = new DBFField();
        fields[0].setName( "emp_code");
        fields[0].setDataType( DBFField.FIELD_TYPE_C);
        fields[0].setFieldLength( 10);

        fields[1] = new DBFField();
        fields[1].setName( "emp_name");
        fields[1].setDataType( DBFField.FIELD_TYPE_C);
        fields[1].setFieldLength( 20);

        fields[2] = new DBFField();
        fields[2].setName( "salary");
        fields[2].setDataType( DBFField.FIELD_TYPE_N);
        fields[2].setFieldLength( 12);
        fields[2].setDecimalCount( 2);

        DBFWriter writer = new DBFWriter();
        writer.setFields( fields);

        // now populate DBFWriter
        //

        Object rowData[] = new Object[3];
        rowData[0] = "1000";
        rowData[1] = "John";
        rowData[2] = new Double( 5000.00);

        writer.addRecord( rowData);

        rowData = new Object[3];
        rowData[0] = "1001";
        rowData[1] = "Lalit";
        rowData[2] = new Double( 3400.00);

        writer.addRecord( rowData);

        rowData = new Object[3];
        rowData[0] = "1002";
        rowData[1] = "Rohit";
        rowData[2] = new Double( 7350.00);

        writer.addRecord( rowData);

        FileOutputStream fos = new FileOutputStream( path);
        writer.write( fos);
        fos.close();
    }

}
