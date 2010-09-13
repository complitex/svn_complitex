package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 17:43:11
 */
public enum TarifDBF {
    T11_DATA_T(String.class, 10),
    T11_DATA_E(String.class, 10),
    T11_DATA_R(String.class, 10),
    T11_MARK(Integer.class, 3),
    T11_TARN(Integer.class, 6),
    T11_CODE1(Integer.class, 3),
    T11_CODE2(Integer.class, 6),
    T11_COD_NA(String.class, 40),
    T11_CODE3(Integer.class, 6),
    T11_NORM_U(Double.class, 19, 10),
    T11_NOR_US(Double.class, 19, 10),
    T11_CODE_N(Integer.class, 3),
    T11_COD_ND(Integer.class, 3),
    T11_CD_UNI(Integer.class, 3),
    T11_CS_UNI(Double.class, 19, 10),
    T11_NORM(Double.class, 19, 10),
    T11_NRM_DO(Double.class, 19, 10),
    T11_NRM_MA(Double.class, 19, 10),
    T11_K_NADL(Double.class, 19, 10);

    private Class type;
    private int length;
    private int scale = 0;

    TarifDBF(Class type, int length, int scale) {
        this.type = type;
        this.length = length;
        this.scale = scale;
    }

    TarifDBF(Class type, int length) {
        this.type = type;
        this.length = length;
    }

    TarifDBF(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }
}
