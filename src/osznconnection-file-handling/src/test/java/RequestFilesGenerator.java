import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.junit.Test;
import org.xBaseJ.DBF;
import org.xBaseJ.Util;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.Field;
import org.xBaseJ.fields.NumField;
import org.xBaseJ.xBaseJException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.09.2010 13:12:18
 */
public class RequestFilesGenerator{
    public static void main(String... arg) throws IOException {
        //generatePayment("C:\\Anatoly\\Java\\org.complitex.osznconnection\\storage", "1760", "01", 1000);
        a2("C:\\Anatoly\\Java\\org.complitex.osznconnection\\storage\\test.dbf");
    }

    private static void generatePayment(String dir, String code, String month, int count){
        try {
            DBF dbf = new DBF(dir + "\\" + RequestFile.PAYMENT_FILES_PREFIX + code + month + ".DBF", true);
//
            Random random = new Random();
//
            Map<PaymentDBF, Field> fields = new HashMap<PaymentDBF, Field>();
//
            for (PaymentDBF paymentDBF : PaymentDBF.values()){
                Field field = null;

                if (paymentDBF.getType().equals(String.class)){
                    field = new CharField(paymentDBF.name(), paymentDBF.getLength());
                }else if (paymentDBF.getType().equals(Integer.class) || paymentDBF.getType().equals(Double.class)){
                    field = new NumField(paymentDBF.name(), paymentDBF.getLength(), paymentDBF.getScale());
                }else if (paymentDBF.getType().equals(Date.class)){
                    field = new DateField(paymentDBF.name());
                }

                fields.put(paymentDBF, field);
                dbf.addField(field);
            }

            for (int i = 0; i < count; ++i){
                for (PaymentDBF paymentDBF : PaymentDBF.values()){
                    Field field = fields.get(paymentDBF);

                    if (paymentDBF.getType().equals(String.class)){
                        String s = paymentDBF.name() + "::" + UUID.randomUUID().toString();
                        field.put(s.substring(0, paymentDBF.getLength()));
                    }else if (paymentDBF.getType().equals(Integer.class) || paymentDBF.getType().equals(Double.class)){
                        Double d = random.nextDouble()*paymentDBF.getLength()*10;
                        field.put(d.toString());
                    }else if (paymentDBF.getType().equals(Date.class)){
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date(new Date().getTime() - random.nextInt(1000000000)));
                        ((DateField)field).put(c);
                    }
                }

                dbf.write();
            }

            dbf.close();

        } catch (xBaseJException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
