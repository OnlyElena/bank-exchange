package beans;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/20/16
 * Time: 5:11 PM
 */
/**
 * ПОСТАНОВЛЕНИЕ об обращении взыскания на денежные средства должника,
 * находящиеся в банке или иной кредитной организации

 * @author: OnlyElena@mail.ru
 * Date: 1/29/14
 * Time: 10:07 AM
 */
public class PostMoneyBanks {

    /*ИД документа в БД*/
    Long id;

    String barcode;

    /*2 - взыскание, 1 - арест, 3 - снятие ареста, 5 - прекращение ИД*/
    String acttype;

    /*Номер постановления об обращении взыскаяния*/
    String actNumber;

    /*Дата постановления об обращении взыскаяния*/
    String actDate;

    /*Номер ИП*/
    String execProcNumber;

    /*Номер ИП(старый) = регистрацонному номеру ИД*/
    String execOldProcNumber;

    /*Дата возбуждения ИП*/
    String execProcDate;

    /*Очередность взыскания*/
    String  priority;

    /*Номер исполнительного документа*/
    String execActNum;

    /*Дата исполнительного документа*/
    String execActDate;

    /*Наименование органа выдавшего ИД*/
    String execActInitial;

    /*Адрес органа выдавшего ИД*/
    String execActInitialAddr;

    /*ФИО пристава*/
    String bailiff;

    /*Сумма долга*/
    String summ;

    /*Наименование взыскателя*/
    String creditorName;

    /*Адрес взыскателя*/
    String creditorAddress;

    /*Номер счета*/
    String accountNumber;

    /*Должник*/
    String debtorFirstName; //Имя
    String debtorLastName; //Фамилия
    String debtorSecondName; //Отчество

    /*Должник организация*/
    Long ENTITY_TYPE;
    String DolgnikOrg;
    String DolgnikInn;
    String DolgnikKpp;

    /*Место рождения должника*/
    String debtorBornAddres;

    /*прописка должника*/
    String debtorAddres;

    /*Год рождения должника*/
    String debtorBirthYear;

    String debtorBirth;

    String osbNumber;

    String accountCurreny;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getActtype() {
        return acttype;
    }

    public void setActtype(String acttype) {
        if (acttype != null && acttype.length() > 100){acttype = "";
        } else if (acttype.equals("O_IP_ACT_GACCOUNT_MONEY")) {acttype = "2";
        } else if (acttype.equals("O_IP_ACT_ARREST_ACCMONEY")) {acttype = "4";
        } else if (acttype.equals("O_IP_ACT_ENDARREST")) {acttype = "3";
        } else if (acttype.equals("O_IP_ACT_GETCURRENCY")) {acttype = "2";
        } else if (acttype.equals("O_IP_ACT_ENDGACCOUNT_MONEY")) {acttype = "5";
        } else  {acttype = acttype;}
        this.debtorAddres = debtorAddres;
        this.acttype = acttype;
    }

    public String getActNumber() {
        return actNumber;
    }

    public void setActNumber(String actNumber) {
        this.actNumber = actNumber;
    }

    public String getActDate() {
        return actDate;
    }

    public void setActDate(String actDate) {
        this.actDate = actDate;
    }

    public String getExecProcNumber() {
        return execProcNumber;
    }

    public void setExecProcNumber(String execProcNumber) {
        this.execProcNumber = execProcNumber;
    }

    public String getExecOldProcNumber() {
        return execOldProcNumber;
    }

    public void setExecOldProcNumber(String execOldProcNumber) {
        this.execOldProcNumber = execOldProcNumber;
    }

    public String getExecProcDate() {
        return execProcDate;
    }

    public void setExecProcDate(String execProcDate) {
        this.execProcDate = execProcDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String  priority) {
        this.priority = priority;
    }

    public String getExecActNum() {
        return execActNum;
    }

    public void setExecActNum(String execActNum) {
        this.execActNum = execActNum;
    }

    public String getExecActDate() {
        return execActDate;
    }

    public void setExecActDate(String execActDate) {
        this.execActDate = execActDate;
    }

    public String getExecActInitial() {
        return execActInitial;
    }

    public void setExecActInitial(String execActInitial) {
        this.execActInitial = execActInitial;
    }

    public String getExecActInitialAddr() {
        return execActInitialAddr;
    }

    public void setExecActInitialAddr(String execActInitialAddr) {
        this.execActInitialAddr = execActInitialAddr;
    }

    public String getBailiff() {
        return bailiff;
    }

    public void setBailiff(String bailiff) {
        this.bailiff = bailiff;
    }

    public String getSumm() {
        return summ;
    }

    public void setSumm(String summ) {
        this.summ = summ;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        //заменяем перевод каретки на пробел т.к. банк это воспринимает за ошибку.
        if (creditorName != null) {
            creditorName = creditorName.trim().replace("\n", " ");
            creditorName = creditorName.trim().replace("  ", " ");
        } else {
            this.creditorName = null;
        }
        this.creditorName = creditorName;

    }

    public String getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(String creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public Long getENTITY_TYPE() {
        return ENTITY_TYPE;
    }

    public void setENTITY_TYPE(Long ENTITY_TYPE) {
        this.ENTITY_TYPE = ENTITY_TYPE;
    }

    public String getDolgnikOrg() {
        return DolgnikOrg;
    }

    public void setDolgnikOrg(String dolgnikOrg) {
        DolgnikOrg = dolgnikOrg;
    }

    public String getDolgnikInn() {
        return DolgnikInn;
    }

    public void setDolgnikInn(String dolgnikInn) {
        DolgnikInn = dolgnikInn;
    }

    public String getDolgnikKpp() {
        return DolgnikKpp;
    }

    public void setDolgnikKpp(String dolgnikKpp) {
        DolgnikKpp = dolgnikKpp;
    }

    public String getDebtorBornAddres() {
        return debtorBornAddres;
    }

    public void setDebtorBornAddres(String debtorBornAddres) {
        if (debtorBornAddres != null && debtorBornAddres.length() > 100) {
            debtorBornAddres = debtorBornAddres.replace(", ,", ",");
            debtorBornAddres = debtorBornAddres.replaceAll(", ", ",");
            debtorBornAddres = debtorBornAddres.replace(",с. ", ",с.");
            debtorBornAddres = debtorBornAddres.replace(",ул. ", ",ул.");
            debtorBornAddres = debtorBornAddres.replace(",д. ", ",д.");
            debtorBornAddres = debtorBornAddres.replace(",кв. ", ",кв.");
            debtorBornAddres = debtorBornAddres.replace(",п. ", ",п.");
            debtorBornAddres = debtorBornAddres.replace("Иркутская обл.", "Ирк.обл.");
            debtorBornAddres = debtorBornAddres.replace("Иркутская обл", "Ирк.обл.");
            debtorBornAddres = debtorBornAddres.replace("Иркутский р-он.", "Ирк. р-он");
            debtorBornAddres = debtorBornAddres.replace("Россия,округ.", "");
            debtorBornAddres = debtorBornAddres.replace("Садоводство", "Сад-во");
            if (debtorBornAddres.length() < 100) {
//                System.out.println(debtorAddres.length() + "\tADDRESS: " + debtorAddres);
            } else {
                debtorBornAddres = debtorBornAddres.substring(debtorBornAddres.length()-99, debtorBornAddres.length());
            }
        }
        this.debtorBornAddres = debtorBornAddres;
    }

    public String getDebtorBirthYear() {
        return debtorBirthYear;
    }

    public void setDebtorBirthYear(String debtorBirthYear) {
        this.debtorBirthYear = debtorBirthYear;
    }

    public String getDebtorAddres() {
        return debtorAddres;
    }

    public void setDebtorAddres(String debtorAddres) {
        if (debtorAddres != null && debtorAddres.length() > 100) {
            debtorAddres = debtorAddres.replace(", ,", ",");
            debtorAddres = debtorAddres.replaceAll(", ", ",");
            debtorAddres = debtorAddres.replace(",с. ", ",с.");
            debtorAddres = debtorAddres.replace(",ул. ", ",ул.");
            debtorAddres = debtorAddres.replace(",д. ", ",д.");
            debtorAddres = debtorAddres.replace(",кв. ", ",кв.");
            debtorAddres = debtorAddres.replace(",п. ", ",п.");
            debtorAddres = debtorAddres.replace("Иркутская обл.", "Ирк.обл.");
            debtorAddres = debtorAddres.replace("Иркутская обл", "Ирк.обл.");
            debtorAddres = debtorAddres.replace("Иркутский р-он.", "Ирк. р-он");
            debtorAddres = debtorAddres.replace("Россия,округ.", "");
            debtorAddres = debtorAddres.replace("Садоводство", "Сад-во");
            if (debtorAddres.length() < 100) {
//                System.out.println(debtorAddres.length() + "\tADDRESS: " + debtorAddres);
            } else {
                debtorAddres = debtorAddres.substring(debtorAddres.length()-99, debtorAddres.length());
            }
        }
        this.debtorAddres = debtorAddres;
    }

    public String getDebtorBirth() {
        return debtorBirth;
    }

    public void setDebtorBirth(String debtorBirth) {
        this.debtorBirth = debtorBirth;
    }

    public String getOsbNumber() {
        return osbNumber;
    }

    public void setOsbNumber(String osbNumber) {
        this.osbNumber = osbNumber;
    }

    public String getAccountCurreny() {
        return accountCurreny;
    }

    public void setAccountCurreny(String accountCurreny) {
        String currency = accountCurreny;
        if (currency == null) {
            accountCurreny="";
        } else if (currency.toLowerCase().contains("рубл")) {
//            id=1,4
            accountCurreny = ("RUB");
        } else if (currency.contains("ПРИЗНАК РОССИЙСКОГО РУБЛЯ")) {
//            id=1,4
            accountCurreny = ("RUB");
        } else if (currency.toLowerCase().contains("россий")) {
//            id=1,4
            accountCurreny = ("RUB");
        } else if (currency.toLowerCase().contains("евро")) {
//            id=2
            accountCurreny = ("EUR");
        } else if (currency.toLowerCase().contains("доллар сша")) {
//            id=3
            accountCurreny = ("USD");
        } else {
            //валюта, которую не обрабатываем
            accountCurreny="";
        }
        this.accountCurreny = accountCurreny;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("| id=").append(id);
        sb.append("| barcode=").append(barcode);
        sb.append("| actype=").append(acttype);
        sb.append("| actNumber=").append(actNumber);
        sb.append("| actDate=").append(actDate);
        sb.append("| execProcNumber=").append(execProcNumber);
        sb.append("| execProcDate=").append(execProcDate);
        sb.append("| priority=").append(priority);
        sb.append("| execActNum=").append(execActNum);
        sb.append("| execActDate=").append(execActDate);
        sb.append("| execActInitial=").append(execActInitial);
        sb.append("| execActInitialAddr=").append(execActInitialAddr);
        sb.append("| bailiff=").append(bailiff);
        sb.append("| summ=").append(summ);
        sb.append("| creditorName=").append(creditorName);
        sb.append("| creditorAddress=").append(creditorAddress);
        sb.append("| accountNumber=").append(accountNumber);
        sb.append("| debtorFirstName=").append(debtorFirstName);
        sb.append("| debtorLastName=").append(debtorLastName);
        sb.append("| debtorSecondName=").append(debtorSecondName);
        sb.append("| debtorBornAddres=").append(debtorBornAddres);
        sb.append("| debtorAddres=").append(debtorAddres);
        sb.append("| debtorBirthYear=").append(debtorBirthYear);
        sb.append("| debtorBirth=").append(debtorBirth);

        return sb.toString();
    }
    public String toStringSber() {
        return "| osbnumber=" + osbNumber +
                "| accountCurreny=" + accountCurreny +
                "| oldnumber=" + execOldProcNumber;
    }
}
