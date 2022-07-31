package org.kramadi.bot.look4tender;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.kramadi.bot.MySQL.HibernateUtil;
import org.kramadi.bot.MySQL.SearchEntity;
import org.kramadi.bot.MySQL.TenderEntity;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class Search {

    public static boolean execute(SearchEntity search) {
//инициализация обработчика скриптов nashorn
        boolean changed = false;
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
//передача скрипта
            engine.eval(search.getPlatformByPlatformId().getScript());
            Invocable invocable = (Invocable) engine;

//вызов метода init с параметром keyword из js
            invocable.invokeFunction("init", search.getKeyword());

//получение результатов поиска (тендеров) при вызове функции searchTenders
            ArrayList<ScriptObjectMirror> tenders = (ArrayList<ScriptObjectMirror>) invocable.invokeFunction("searchTenders");

            for (ScriptObjectMirror s : tenders) {
                ArrayList<TenderEntity> tenderList = HibernateUtil.selectTender("from TenderEntity where id = '" +
                            s.getMember("id") + "' and searchBySearchId.id = " + search.getId());


                if (tenderList.size()==0) {
                    TenderEntity newTender = new TenderEntity();
                    newTender.setSubject(s.get("subject").toString());
                    newTender.setOrganization(s.get("organization").toString());
                    newTender.setPrice(s.get("price").toString());
                    newTender.setStatus(s.get("status").toString());
                    newTender.setStartDate(s.get("start_date").toString());
                    newTender.setEndDate(s.get("end_date").toString());
                    newTender.setUrl(s.get("url").toString());
                    newTender.setFoundDate(Date.valueOf(LocalDate.now()));
                    newTender.setUpdateDate(Date.valueOf(LocalDate.now()));
                    newTender.setSearchBySearchId(search);
                    HibernateUtil.insert(newTender);
                    changed = true;
                }
                else {
                    TenderEntity tender = tenderList.get(0);
                    if (!tender.getPrice().equals(s.get("price").toString())) {
                        tender.setPrice(s.get("price").toString());
                        changed = true;
                    }
                    if (!tender.getSubject().equals(s.get("subject").toString())) {
                        tender.setSubject(s.get("subject").toString());
                        changed = true;
                    }
                    if (!tender.getOrganization().equals(s.get("organization").toString())) {
                        tender.setOrganization(s.get("organization").toString());
                        changed = true;
                    }
                    if (!tender.getStatus().equals(s.get("status").toString())) {
                        tender.setStatus(s.get("status").toString());
                        changed = true;
                    }if (!tender.getUrl().equals(s.get("url").toString())){
                        tender.setUrl(s.get("url").toString());
                        changed = true;
                    }if
                    (!tender.getStartDate().equals(s.get("start_date").toString())){
                        tender.setStartDate(s.get("start_date").toString());
                        changed = true;
                    }if
                    (!tender.getEndDate().equals(s.get("end_date").toString())) {
                        tender.setEndDate(s.get("end_date").toString());
                        changed = true;
                    }if (changed){
                        tender.setUpdateDate(Date.valueOf(LocalDate.now()));
                        HibernateUtil.update(tender);
                    }
                }
            }

        } catch (ScriptException e) { e.printStackTrace();
        } catch (NoSuchMethodException e) { e.printStackTrace();
        }
        return changed;
    }
}
