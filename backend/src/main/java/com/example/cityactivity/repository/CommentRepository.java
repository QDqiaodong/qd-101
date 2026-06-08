package com.example.cityactivity.repository;

import com.example.cityactivity.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replyToUser " +
            "WHERE c.activity.id = :activityId AND c.parent IS NULL " +
            "ORDER BY c.isPinned DESC, c.createdAt DESC")
    List<Comment> findRootCommentsByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replyToUser " +
            "WHERE c.activity.id = :activityId AND c.parent IS NULL AND c.category = :category " +
            "ORDER BY c.isPinned DESC, c.createdAt DESC")
    List<Comment> findRootCommentsByActivityIdAndCategory(@Param("activityId") Long activityId, @Param("category") String category);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.replyToUser " +
            "WHERE c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    long countByActivityId(Long activityId);

    long countByActivityIdAndCategory(Long activityId, String category);

    @Query("SELECT c.category, COUNT(c) FROM Comment c WHERE c.activity.id = :activityId AND c.parent IS NULL " +
            "AND c.category IS NOT NULL GROUP BY c.category")
    List<Object[]> countByActivityIdGroupByCategory(@Param("activityId") Long activityId);
}
