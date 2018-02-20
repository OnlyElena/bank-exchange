package beans;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/14/16
 * Time: 3:33 PM
 */
/**
 * @author: Andrey Panov
 * Date: 6/13/12
 * Time: 11:58 AM
 */
public class OSP {

    String territory;

    String department;

    String name;

    String fullname;

    String bankname;

    String kpp;

    String inn;

    String okato;

    String bik;

    //лицевой счет
    String ls;

    String account;

    String receivTitle;

    String address;

    String orfkKod;

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        //заменяем длинные названия, на короткие, где это не сделано в отделах
        if (name != null) {
//            name = name.replace("УФССП России по Иркутской области", "");
//            name = name.replace("УФССП по Иркутской области", "");
//            name = name.replace("Управления Федеральной службы судебных приставов по Иркутской области", "");
            name = name.trim().replace("  ", " ").toLowerCase().replace("управления Федеральной службы судебных приставов ", "УФССП");
            name = name.trim().replace("  ", " ").toLowerCase().replace("районный отдел судебных приставов", "РОСП");
            name = name.trim().replace("  ", " ").toLowerCase().replace("отдел судебных приставов", "ОСП");
            name = name.trim().replace("  ", " ").toLowerCase().replace("специализированный отдел исполнения судебных решений и актов специально уполномоченных органов", "СОИСР и АСУО");
            name = name.trim();
        } else {
            this.name = null;
        }
        this.name = name;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    public String getBik() {
        return bik;
    }

    public void setBik(String bik) {
        this.bik = bik;
    }

    public String getLs() {
        return ls;
    }

    public void setLs(String ls) {
        this.ls = ls;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getReceivTitle() {
        return receivTitle;
    }

    public void setReceivTitle(String receivTitle) {
        this.receivTitle = receivTitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOrfkKod() {
        return orfkKod;
    }

    public void setOrfkKod(String orfkKod) {
        this.orfkKod = orfkKod;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("| territory=").append(territory);
        sb.append("| department=").append(department);
        sb.append("| name=").append(name);
//        sb.append("| fullname=").append(fullname);
        sb.append("| bankname=").append(bankname);
        sb.append("| kpp=").append(kpp);
        sb.append("| inn=").append(inn);
        sb.append("| okato=").append(okato);
        sb.append("| bik=").append(bik);
        sb.append("| ls=").append(ls);
        sb.append("| account=").append(account);
        sb.append("| receivTitle=").append(receivTitle);
        sb.append("| address=").append(address);
        sb.append("| kbk=null");
        sb.append("| bankBranch=null");
        sb.append("| bankAgency=null");

        return sb.toString();
    }
}