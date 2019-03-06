/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.changes.ChangeEvent;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import static java.util.Arrays.asList;

@Test
public class ChangesTrackerCommandTests {
   private ChangesTracker tracker;
   private RegisterCommand register;
   private PollCommand poll;

   public void testRegisterDelegatesToService() throws Exception {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("listenerId", "123");
      parameters.put("fileName", "directory");
      parameters.put("recursive", "true");
      register.execute(parameters);

      verify(tracker).registerPath("123", new File("directory"), true);
   }

   public void testChangesAreSerializedToCommaSeparatedList() throws Exception {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("listenerId", "123");
      String baseDir = new File("").getAbsolutePath();
      when(tracker.pollChanges("123")).thenReturn(asList(
               new ChangeEvent(new File("file1"), Created),
               new ChangeEvent(new File("some/file2"), Modified),
               new ChangeEvent(new File("some/file3"), Deleted)));

      String reply = poll.execute(parameters);

      assertEquals(reply, baseDir+"/file1|Created,"+baseDir+"/some/file2|Modified,"+baseDir+"/some/file3|Deleted");
   }

   @BeforeMethod
   protected void setUp() {
      tracker = mock(ChangesTracker.class);
      register = new RegisterCommand(tracker);
      poll = new PollCommand(tracker);
   }
}
