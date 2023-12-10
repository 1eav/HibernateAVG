import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Получение количества студентов на каждом курсе
        CriteriaQuery<Object[]> countStudentsQuery = builder.createQuery(Object[].class);
        Root<Course> courseRoot = countStudentsQuery.from(Course.class);
        countStudentsQuery.multiselect(
                courseRoot.get("name"),
                builder.count(courseRoot.join("students"))
        );
        countStudentsQuery.groupBy(courseRoot.get("name"));
        List<Object[]> studentCountResult = session.createQuery(countStudentsQuery).getResultList
                ();
        for (Object[] result : studentCountResult) {
            String courseName = (String) result[0];
            long studentsCount = (long) result[1];
            System.out.println("Курс: " + courseName + ", Кол-во студентов: " + studentsCount);
        }

        // Получение списка курсов для конкретного студента
        CriteriaQuery<Course> studentCoursesQuery = builder.createQuery(Course.class);
        Root<Course> studentCoursesRoot = studentCoursesQuery.from(Course.class);
        Join<Course, Student> studentJoin = studentCoursesRoot.join("students");
        studentCoursesQuery.select(studentCoursesRoot);
        studentCoursesQuery.where(builder.equal(studentJoin.get("name"), "Блаженов Артем"));
        List<Course> resultList = session.createQuery(studentCoursesQuery).getResultList();
        resultList.forEach(course -> System.out.println("Курсы Блаженова Артема " + course.getName()));

        transaction.commit();
        session.close();
        sessionFactory.close();
    }
}