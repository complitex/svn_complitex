package org.complitex.dictionary.service.exception;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.09.12 15:37
 */
public class ProcessRowException extends AbstractException{
    private int row = 0;

    public ProcessRowException(int row){
        super("Ошибка процесс в строке {0}", row);

        this.row = row;
    }

    public ProcessRowException(String pattern, Object... arguments) {
        super(pattern, arguments);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
