package org.example;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        HWND prevFg = null;
        while (!Thread.interrupted()) {
            HWND fg = Objects.requireNonNullElse(User32.INSTANCE.GetForegroundWindow(), prevFg);

            //doesn't print the name if it's the same as before
            if (fg.equals(prevFg)) {
                continue;
            }
            String fgImageName = getImageName(fg);
            String x = "\\\\";
            if(fgImageName!=null){
                //splits the directory and cuts the ".exe" at the end
                System.out.println(fgImageName.split((x))[fgImageName.split(x).length-1].substring(0, fgImageName.split((x))[fgImageName.split(x).length-1].length()-4));
            }else{
                Thread.currentThread().interrupt();
            }
            prevFg = fg;
        }
    }

    private static String getImageName(HWND window) {
        IntByReference procId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(window, procId);

        HANDLE procHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
                false,
                procId.getValue()
        );

        char[] buffer = new char[4096];
        IntByReference bufferSize = new IntByReference(buffer.length);
        boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(procHandle, 0, buffer, bufferSize);

        Kernel32.INSTANCE.CloseHandle(procHandle);

        return success ? new String(buffer, 0, bufferSize.getValue()) : null;
    }
}