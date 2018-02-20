package beans;

/**
 * @author: OnlyElena@mail.ru
 * Date: 7/9/16
 * Time: 11:46 PM
 */
/**
 * Уведомление Банка о принятии / не принятии к исполнению Электронного постановления
 * (закладка №3 в постановлении)
 * <p/>
 * Маска файлов: UKKKKDDM.NSS
 * <p/>
 * Статусы ни о чем не говорят :)
 * status 0:	Принят к исполнению – постановление передано на обработку
 * status 3:	возврат – дубликат ИД - постановление не принято к исполнению
 * status 7:	Возврат – несоответствие Соц. данных – постановление не принято к исполнению
 * status 2:	Возврат - отсутствуютлибо не корректны  обязательные реквизиты - постановление не принято к исполнению; Проблемные реквизиты: (Номер ИД; )
 * status 2:	Возврат - отсутствуютлибо не корректны  обязательные реквизиты - постановление не принято к исполнению
 * status 4:	Возврат – счет закрыт - постановление не принято к исполнению
 * status 1:	Возврат – недостаточно реквизитов - постановление не принято к исполнению
 * status 2:	Возврат - отсутствуютлибо не корректны  обязательные реквизиты - постановление не принято к исполнению; Проблемные реквизиты: (Номер ИД; Дата ИД; )
 * status 5:	Возврат – счет не найден - постановление не принято к исполнению
 * status 2:	Возврат - отсутствуютлибо не корректны  обязательные реквизиты - постановление не принято к исполнению; Проблемные реквизиты: (Дата ИД; )
 * status 7:	Возврат – вкладчик умер – постановление не принято к исполнению
 * status 2:	Возврат - отсутствуютлибо не корректны  обязательные реквизиты - постановление не принято к исполнению; Проблемные реквизиты: (Сумма долга по ИД; )
 * status 4:	null
 * <p/>
 * <p/>
 * User: Andrey V. Panov
 * Date: 8/13/12
 */
public class NotifBeanBanks {
    String id;
    String execProcNumber;
    String debtorFirstName;
    String debtorLastName;
    String debtorSecondName;
    String accountNumber;
    String summ;
    String procNumberState;
    String status;
    String ActNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExecProcNumber() {
        return execProcNumber;
    }

    public void setExecProcNumber(String execProcNumber) {
        this.execProcNumber = execProcNumber;
    }

    public String getDebtorFirstName() {
        return debtorFirstName;
    }

    public void setDebtorFirstName(String debtorFirstName) {
        this.debtorFirstName = debtorFirstName;
    }

    public String getDebtorLastName() {
        return debtorLastName;
    }

    public void setDebtorLastName(String debtorLastName) {
        this.debtorLastName = debtorLastName;
    }

    public String getDebtorSecondName() {
        return debtorSecondName;
    }

    public void setDebtorSecondName(String debtorSecondName) {
        this.debtorSecondName = debtorSecondName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSumm() {
        return summ;
    }

    public void setSumm(String summ) {
        this.summ = summ;
    }

    public String getProcNumberState() {
        return procNumberState;
    }

    public void setProcNumberState(String procNumberState) {
        this.procNumberState = procNumberState;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActNumber() {
        return ActNumber;
    }

    public void setActNumber(String actNumber) {
        ActNumber = actNumber;
    }

    @Override
    public String toString() {
        String toString = "Постановление: " +
                "id='" + id + '\'' +
                ", № ='" + ActNumber + '\'' +
                ",\nИП № ='" + execProcNumber + '\'' +
                ",\nДолжник: Фамилия ='" + debtorLastName + '\'' +
                " Имя ='" + debtorFirstName + '\'' +
                " Отчество ='" + debtorSecondName + '\'' +
                ",\nНомер счета ='" + accountNumber + '\'' +
                ", Сумма обращения взыскания ='" + summ + '\'' +
                ",\nКод статуса постановления ='" + status + '\'' +
                ", Статус постановеления ='" + procNumberState + '\''
                ;

        if (toString.length()>512) return toString.substring(512);
        return toString;
    }


}