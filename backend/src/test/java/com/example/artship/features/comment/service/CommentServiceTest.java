package com.example.artship.features.comment.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.comment.model.Comment;
import com.example.artship.entities.comment.repository.CommentRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.comment.dto.CommentDto;
import com.example.artship.features.comment.mapper.CommentMapper;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArtRepository artRepository;

    @Mock
    private UserRepository userRepository;

    private CommentService commentService;

    private Art art;
    private User user;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
                commentRepository,
                artRepository,
                userRepository,
                new CommentMapper()
        );

        art = Mockito.mock(Art.class);
        user = Mockito.mock(User.class);
    }

    private Comment createComment(Long id, String text) {
        Comment comment = new Comment(text, art, user);
        comment.setId(id);
        return comment;
    }

    @Test
    void createCommentShouldSaveAndReturnComment() {
        when(art.getId()).thenReturn(1L);
        when(user.getId()).thenReturn(1L);
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = commentService.createComment(
                "Great artwork!",
                1L,
                1L
        );

        assertNotNull(result);
        assertEquals("Great artwork!", result.getText());
        assertEquals(1L, result.getArtId());
        assertEquals(1L, result.getUserId());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createCommentShouldTrimText() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = commentService.createComment(
                "  Nice work!  ",
                1L,
                1L
        );

        assertEquals("Nice work!", result.getText());
    }

    @Test
    void createCommentShouldThrowWhenTextIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(" ", 1L, 1L)
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    void createCommentShouldThrowWhenArtDoesNotExist() {
        when(artRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(
                        "Comment",
                        999L,
                        1L
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createCommentShouldThrowWhenUserDoesNotExist() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(
                        "Comment",
                        1L,
                        999L
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createReplyShouldSaveReply() {
        Comment parent = createComment(10L, "Parent comment");
        when(art.getId()).thenReturn(1L);

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(parent));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = commentService.createReply(
                "Reply",
                10L,
                1L
        );

        assertNotNull(result);
        assertEquals("Reply", result.getText());
        assertEquals(1L, result.getArtId());
        assertEquals(10L, result.getParentCommentId());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createReplyShouldThrowWhenParentDoesNotExist() {
        when(commentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createReply(
                        "Reply",
                        999L,
                        1L
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createReplyShouldThrowWhenParentIsHidden() {
        Comment parent = createComment(10L, "Hidden comment");
        parent.setHidden(true);

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(parent));

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createReply(
                        "Reply",
                        10L,
                        1L
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentByIdShouldReturnComment() {
        Comment comment = createComment(1L, "Comment");

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        CommentDto result = commentService.getCommentById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Comment", result.getText());
    }

    @Test
    void getCommentByIdShouldThrowWhenCommentDoesNotExist() {
        when(commentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.getCommentById(999L)
        );
    }

    @Test
    void getCommentsByArtIdShouldReturnVisibleComments() {
        Comment comment = createComment(1L, "Visible comment");

        when(artRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByArtIdAndHiddenFalse(1L))
                .thenReturn(List.of(comment));

        List<CommentDto> result
                = commentService.getCommentsByArtId(1L);

        assertEquals(1, result.size());
        assertEquals("Visible comment", result.get(0).getText());
    }

    @Test
    void getCommentsByArtIdShouldThrowWhenArtDoesNotExist() {
        when(artRepository.existsById(999L)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.getCommentsByArtId(999L)
        );
    }

    @Test
    void getRootCommentsByArtIdShouldReturnRootComments() {
        Comment comment = createComment(1L, "Root comment");

        when(artRepository.existsById(1L)).thenReturn(true);
        when(commentRepository
                .findByArtIdAndParentCommentIsNullAndHiddenFalse(1L))
                .thenReturn(List.of(comment));

        List<CommentDto> result
                = commentService.getRootCommentsByArtId(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getParentCommentId());
    }

    @Test
    void getRepliesShouldReturnOnlyVisibleReplies() {
        Comment parent = createComment(10L, "Parent");

        Comment visibleReply = createComment(11L, "Visible reply");
        visibleReply.setParentComment(parent);

        Comment hiddenReply = createComment(12L, "Hidden reply");
        hiddenReply.setParentComment(parent);
        hiddenReply.setHidden(true);

        when(commentRepository.findById(10L))
                .thenReturn(Optional.of(parent));
        when(commentRepository.findByParentCommentId(10L))
                .thenReturn(List.of(visibleReply, hiddenReply));

        List<CommentDto> result = commentService.getReplies(10L);

        assertEquals(1, result.size());
        assertEquals("Visible reply", result.get(0).getText());
    }

    @Test
    void getCommentsByUserIdShouldReturnOnlyVisibleComments() {
        Comment visible = createComment(1L, "Visible");
        Comment hidden = createComment(2L, "Hidden");
        hidden.setHidden(true);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByUserId(1L))
                .thenReturn(List.of(visible, hidden));

        List<CommentDto> result
                = commentService.getCommentsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("Visible", result.get(0).getText());
    }

    @Test
    void updateCommentShouldUpdateTextWhenUserIsAuthor() {
        when(user.getId()).thenReturn(1L);
        Comment comment = createComment(1L, "Old text");

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = commentService.updateComment(
                1L,
                1L,
                "New text"
        );

        assertEquals("New text", result.getText());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateCommentShouldThrowWhenUserIsNotAuthor() {
        when(user.getId()).thenReturn(1L);
        Comment comment = createComment(1L, "Original text");

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.updateComment(
                        1L,
                        2L,
                        "New text"
                )
        );

        assertEquals("Original text", comment.getText());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateCommentShouldThrowWhenCommentIsHidden() {
        when(user.getId()).thenReturn(1L);
        Comment comment = createComment(1L, "Hidden");
        comment.setHidden(true);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.updateComment(
                        1L,
                        1L,
                        "New text"
                )
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void hideCommentShouldSetHiddenFlag() {
        Comment comment = createComment(1L, "Comment");

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = commentService.hideComment(1L);

        assertTrue(result.isHidden());
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteCommentShouldDeleteCommentAndReplies() {
        when(user.getId()).thenReturn(1L);
        Comment parent = createComment(1L, "Parent");
        Comment reply = createComment(2L, "Reply");
        reply.setParentComment(parent);

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(parent));
        when(commentRepository.findByParentCommentId(1L))
                .thenReturn(List.of(reply));
        when(commentRepository.findByParentCommentId(2L))
                .thenReturn(List.of());

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).delete(reply);
        verify(commentRepository).delete(parent);
    }

    @Test
    void deleteCommentShouldThrowWhenUserIsNotAuthor() {
        when(user.getId()).thenReturn(1L);
        Comment comment = createComment(1L, "Comment");

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.deleteComment(1L, 2L)
        );

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void countCommentsByArtIdShouldReturnCount() {
        when(artRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.countByArtIdAndHiddenFalse(1L))
                .thenReturn(15L);

        long result = commentService.countCommentsByArtId(1L);

        assertEquals(15L, result);
    }

    @Test
    void countCommentsByArtIdShouldThrowWhenArtDoesNotExist() {
        when(artRepository.existsById(999L)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.countCommentsByArtId(999L)
        );

        verify(commentRepository, never())
                .countByArtIdAndHiddenFalse(any());
    }
}
