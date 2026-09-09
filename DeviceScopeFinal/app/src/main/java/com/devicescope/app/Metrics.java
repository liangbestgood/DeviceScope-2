package com.devicescope.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StatFs;
import android.os.SystemHealthManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public final class Metrics {
    public float cpu; public float ram; public float battery; public float power; public float cpuHeadroom=-1; public float gpuHeadroom=-1; public long storageFree; public long storageTotal; public List<Float> cores=new ArrayList<>();
    private long prevTotal=-1, prevIdle=-1; private long[] prevCoreTotal; private long[] prevCoreIdle;
    public static Metrics read(Context c, Metrics old){ Metrics m=old==null?new Metrics():old; m.readCpu(); m.readRam(c); m.readBattery(c); m.readStorage(); m.readHeadroom(c); return m; }
    private void readCpu(){
        try(BufferedReader br=new BufferedReader(new FileReader("/proc/stat"))){String line; long total=0,idle=0; List<Long> ct=new ArrayList<>(),ci=new ArrayList<>(); while((line=br.readLine())!=null){if(line.startsWith("cpu ")){String[] p=line.trim().split("\\s+");for(int i=1;i<p.length;i++)total+=Long.parseLong(p[i]); idle=Long.parseLong(p[4])+Long.parseLong(p[5]);}else if(line.matches("cpu[0-9]+\\s+.*")){String[] p=line.trim().split("\\s+");long t=0;for(int i=1;i<p.length;i++)t+=Long.parseLong(p[i]);ct.add(t);ci.add(Long.parseLong(p[4])+Long.parseLong(p[5]));}} if(prevTotal>0){long dt=total-prevTotal,di=idle-prevIdle;cpu=dt>0?100f*(dt-di)/dt:0;} prevTotal=total;prevIdle=idle; if(prevCoreTotal!=null){cores.clear();for(int i=0;i<ct.size();i++){long dt=ct.get(i)-prevCoreTotal[i],di=ci.get(i)-prevCoreIdle[i];cores.add(dt>0?100f*(dt-di)/dt:0);}} prevCoreTotal=new long[ct.size()];prevCoreIdle=new long[ci.size()];for(int i=0;i<ct.size();i++){prevCoreTotal[i]=ct.get(i);prevCoreIdle[i]=ci.get(i);}}
        }catch(Exception ignored){}
    }
    private void readRam(Context c){ActivityManager am=(ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);ActivityManager.MemoryInfo i=new ActivityManager.MemoryInfo();am.getMemoryInfo(i);ram=100f*(1f-(float)i.availMem/(float)i.totalMem);}
    private void readBattery(Context c){BatteryManager b=(BatteryManager)c.getSystemService(Context.BATTERY_SERVICE);int cap=b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);battery=cap;int cur=b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);int volt=b.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE_NOW);power=cur==Integer.MIN_VALUE||volt==Integer.MIN_VALUE?0f:Math.abs(cur)*Math.abs(volt)/1_000_000_000f;}
    private void readStorage(){StatFs s=new StatFs(android.os.Environment.getDataDirectory().getPath());storageTotal=s.getTotalBytes();storageFree=s.getAvailableBytes();}
    private void readHeadroom(Context c){if(Build.VERSION.SDK_INT>=36){try{SystemHealthManager h=(SystemHealthManager)c.getSystemService(SystemHealthManager.class);cpuHeadroom=h.getCpuHeadroom(new SystemHealthManager.CpuHeadroomParams.Builder().build());gpuHeadroom=h.getGpuHeadroom(new SystemHealthManager.GpuHeadroomParams.Builder().build());}catch(Throwable ignored){}}}
    public String summary(){return String.format("CPU %.0f%%\nRAM %.0f%%\nBattery %.0f%%\nPower %.2f W",cpu,ram,battery,power);}
}
