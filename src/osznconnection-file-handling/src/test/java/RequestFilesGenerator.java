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

import java.io.IOException;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.09.2010 13:12:18
 */
public class RequestFilesGenerator{
    public static void main(String... arg){
        generatePayment("C:\\Anatoly\\Java\\org.complitex.osznconnection\\storage", "1760", "01", 1000);
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

}
