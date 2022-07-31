package org.kramadi.bot.MySQL;

import java.io.File;
import java.util.ArrayList;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
                // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure(
                    new File("src/main/resources/hibernate.cfg.xml")).buildSessionFactory();
        }
        catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static SessionFactory getSessionFactory() {
        if(sessionFactory == null) sessionFactory = buildSessionFactory();
        return sessionFactory;
    }

    public static int insert(DataEntity entity) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        int id = (Integer) session.save(entity);
        session.getTransaction().commit();
        session.close();
        return id;
    }

    public static void update(DataEntity updatedEntity){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(updatedEntity);
        session.getTransaction().commit();
        session.close();
    }

    public static void delete(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        DataEntity platform = (DataEntity) q.list().get(0);
        session.delete(platform);
        session.getTransaction().commit();
        session.close();
    }

    public static void deleteAll(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        q.executeUpdate();
        session.getTransaction().commit();
        session.close();
    }

    public static PlatformEntity selectPlatform(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        PlatformEntity platform = (PlatformEntity) q.list().get(0);
        session.close();
        return platform;
    }

    public static ArrayList<PlatformEntity> selectPlatforms(){
        Session session = HibernateUtil.getSessionFactory().openSession(); session.beginTransaction();
        Query q = session.createQuery("from PlatformEntity");
        ArrayList<PlatformEntity> platforms = (ArrayList<PlatformEntity>) q.list();
        session.close();
        return platforms;
    }

    public static ArrayList<TenderEntity> selectTender(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        ArrayList<TenderEntity> tender = (ArrayList<TenderEntity>) q.list();  //.get(0);
        session.close();
        return tender;
    }

    public static ArrayList<TenderEntity> selectTendersBySearch(int search_id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery("from TenderEntity where searchBySearchId.id = " + search_id);
        ArrayList<TenderEntity> tenders = (ArrayList<TenderEntity>) q.list();
        session.close();
        return tenders;
    }

    public static SearchEntity selectSearch(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        SearchEntity search = (SearchEntity) q.list().get(0);
        session.close();
        return search;
    }

    public static ArrayList<SearchEntity> selectSearchesByUser(int user_id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery("from SearchEntity where userByUserId.id = " + user_id);
        ArrayList<SearchEntity> searches = (ArrayList<SearchEntity>) q.list();
        session.close();
        return searches;
    }

    public static ArrayList<UserEntity> selectUser(String HQL){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(HQL);
        ArrayList<UserEntity> user = (ArrayList<UserEntity>) q.list();
        session.close();

        return user;
    }
}
