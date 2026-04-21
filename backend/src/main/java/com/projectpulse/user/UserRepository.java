package com.projectpulse.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findAllByRoleAndIsActiveTrue(String role);

    @Query(nativeQuery = true, value = """
            SELECT
                u.id          AS id,
                u.first_name  AS firstName,
                u.last_name   AS lastName,
                u.email       AS email,
                s.name        AS sectionName,
                t.name        AS teamName
            FROM users u
            LEFT JOIN section_students ss ON u.id = ss.student_id
            LEFT JOIN sections s          ON ss.section_id = s.id
            LEFT JOIN team_students ts    ON u.id = ts.student_id
            LEFT JOIN teams t             ON ts.team_id = t.id
            WHERE u.id = :id AND u.role = 'STUDENT'
            LIMIT 1
            """)
    Optional<StudentSearchProjection> findStudentById(@Param("id") Long id);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT
                u.id          AS id,
                u.first_name  AS firstName,
                u.last_name   AS lastName,
                u.email       AS email,
                s.name        AS sectionName,
                t.name        AS teamName
            FROM users u
            LEFT JOIN section_students ss ON u.id = ss.student_id
            LEFT JOIN sections s          ON ss.section_id = s.id
            LEFT JOIN team_students ts    ON u.id = ts.student_id
            LEFT JOIN teams t             ON ts.team_id = t.id
            WHERE u.role = 'STUDENT'
              AND (:firstName  IS NULL OR u.first_name LIKE CONCAT('%', :firstName,  '%'))
              AND (:lastName   IS NULL OR u.last_name  LIKE CONCAT('%', :lastName,   '%'))
              AND (:email      IS NULL OR u.email      LIKE CONCAT('%', :email,      '%'))
              AND (:sectionName IS NULL OR s.name      LIKE CONCAT('%', :sectionName,'%'))
              AND (:teamName   IS NULL OR t.name       LIKE CONCAT('%', :teamName,   '%'))
              AND (:sectionId  IS NULL OR s.id = :sectionId)
              AND (:teamId     IS NULL OR t.id = :teamId)
            ORDER BY ISNULL(s.name), s.name DESC, u.last_name ASC
            """)
    List<StudentSearchProjection> searchStudents(
            @Param("firstName")   String firstName,
            @Param("lastName")    String lastName,
            @Param("email")       String email,
            @Param("sectionName") String sectionName,
            @Param("teamName")    String teamName,
            @Param("sectionId")   Long sectionId,
            @Param("teamId")      Long teamId
    );

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM section_students WHERE student_id = :id")
    void deleteSectionMemberships(@Param("id") Long id);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM team_students WHERE student_id = :id")
    void deleteTeamMemberships(@Param("id") Long id);
}
