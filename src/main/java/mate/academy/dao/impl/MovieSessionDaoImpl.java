package mate.academy.dao.impl;

import java.time.LocalDate;
import java.util.List;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {

    @Override
    public MovieSession add(MovieSession movieSession) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert movieSession " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public MovieSession get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> getMovieSessionQuery = session.createQuery("FROM MovieSession ms "
                            + "LEFT JOIN FETCH ms.movie LEFT JOIN FETCH ms.cinemaHall "
                            + "WHERE ms.id = :id", MovieSession.class);
            getMovieSessionQuery.setParameter("id", id);
            return getMovieSessionQuery.getSingleResult();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get a movieSession by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> getMovieSessionQuery = session.createQuery("FROM MovieSession "
                    + "ms LEFT JOIN FETCH ms.movie LEFT JOIN FETCH ms.cinemaHall "
                    + "WHERE ms.movie.id = :movieId AND DATE(ms.showTime) = :showTime",
                    MovieSession.class);
            getMovieSessionQuery.setParameter("movieId", movieId);
            getMovieSessionQuery.setParameter("showTime", date);
            return getMovieSessionQuery.getResultList();
        }
    }
}
