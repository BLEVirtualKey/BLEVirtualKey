package com.example.hxie.myapplication;

import android.widget.TextView;

import org.junit.Test;

import static org.junit.Assert.*;
import com.example.gemaltotest.EseSimulator;
import com.gemalto.virtualkey.api.virtualkey.Virtualkey;
import com.gemalto.virtualkey.api.virtualkey.VirtualkeyException;
import com.gemalto.virtualkey.taadmin.TaAdmin;
import com.gemalto.virtualkey.taadmin.IVirtualkeyAdminNotification;
import com.gto.tee.agentlibrary.proxy.AgentResultCodes;
import com.gto.tee.agentlibrary.proxy.ProgressState;
import com.gto.tee.agentlibrary.Utils.TEE_TYPE;
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}