package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    @Mock
    LeadRepository repo;      // поддельная база

    @Mock
    MailService mail;         // поддельная почта

    @InjectMocks
    LeadController controller; // настоящий контроллер, в который подставлены моки

    @Test
    void existingLead_isNotSavedAgain_andNoMailSent() {
        // Arrange: в "базе" уже есть заявка row-1
        Lead existing = new Lead();
        existing.setExternalId("row-1");
        existing.setName("Ivan");
        when(repo.findByExternalId("row-1")).thenReturn(Optional.of(existing));

        // Act: приходит повторная заявка с тем же externalId
        Lead result = controller.create(Map.of(
                "externalId", "row-1",
                "name", "Petr"
        ));

        // Assert: вернулась старая заявка, ничего не сохранено, письмо не ушло
        assertSame(existing, result);
        assertEquals("Ivan", result.getName());
        verify(repo, never()).save(any());
        verify(mail, never()).notifyNewLead(any());
    }

    @Test
    void newLead_isSaved_andMailSent() {
        // Arrange: заявки row-2 в базе нет
        when(repo.findByExternalId("row-2")).thenReturn(Optional.empty());
        // save() возвращает тот же объект, который ему передали
        when(repo.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Lead result = controller.create(Map.of(
                "externalId", "row-2",
                "name", "Anna",
                "contact", "@anna",
                "message", "Need logo"
        ));

        // Assert: заявка заполнена и сохранена
        assertEquals("row-2", result.getExternalId());
        assertEquals("Anna", result.getName());
        assertEquals("@anna", result.getContact());
        verify(repo, times(1)).save(any(Lead.class));

        // Письмо отправляется в отдельном потоке, поэтому ждём до 1 секунды
        verify(mail, timeout(1000)).notifyNewLead(result);
    }
}