package com.lge.camera.app;

import android.os.AsyncTask;
import com.lge.camera.app.ThumbnailHelper.LoaderTask;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class ProviderExecutor extends Thread implements Executor {
    static final /* synthetic */ boolean $assertionsDisabled = (!ProviderExecutor.class.desiredAssertionStatus());
    private static HashMap<String, ProviderExecutor> sExecutors = new HashMap();
    private static Stack<Comm> stack = new Stack();
    private final LinkedBlockingQueue<Comm> mFakeQueue = new LinkedBlockingQueue();
    private Executor mNonPreemptingExecutor = new C03351();
    private final ArrayList<WeakReference<Preemptable>> mPreemptable = new ArrayList();
    private final LinkedBlockingQueue<Comm> mQueue = new LinkedBlockingQueue();
    private int priority = 1;

    /* renamed from: com.lge.camera.app.ProviderExecutor$1 */
    class C03351 implements Executor {
        static final /* synthetic */ boolean $assertionsDisabled = (!ProviderExecutor.class.desiredAssertionStatus());

        C03351() {
        }

        public void execute(Runnable command) {
            if ($assertionsDisabled || command != null) {
                Comm comm = new Comm(ProviderExecutor.this, null);
                comm.f13p = ProviderExecutor.this.priority;
                comm.f14r = command;
                ProviderExecutor.this.add(comm);
                return;
            }
            throw new AssertionError();
        }
    }

    private class Comm {
        /* renamed from: p */
        int f13p;
        /* renamed from: r */
        Runnable f14r;

        private Comm() {
        }

        /* synthetic */ Comm(ProviderExecutor x0, C03351 x1) {
            this();
        }
    }

    public interface Preemptable {
        void preempt();
    }

    public static ProviderExecutor forAuthority(String authority) {
        ProviderExecutor executor;
        synchronized (sExecutors) {
            executor = (ProviderExecutor) sExecutors.get(authority);
            if (executor == null) {
                executor = new ProviderExecutor();
                executor.setName("ProviderExecutor: " + authority);
                executor.start();
                sExecutors.put(authority, executor);
            }
        }
        return executor;
    }

    private Runnable take() {
        Comm result = null;
        StringBuilder builder = new StringBuilder();
        Iterator<Comm> it = this.mQueue.iterator();
        while (it != null && it.hasNext()) {
            Comm comm = (Comm) it.next();
            if (result == null) {
                result = comm;
            } else if (comm.f13p > result.f13p) {
                result = comm;
            }
            builder.append(comm.f13p);
            builder.append(" ");
        }
        if (result == null) {
            return null;
        }
        CamLog.m7i(CameraConstants.TAG, "[Tile] execute mQueue : " + builder.toString());
        this.mQueue.remove(result);
        return result.f14r;
    }

    private void add(Comm command) {
        int pThirtyCount = 0;
        int pFortyCount = 0;
        int pFiftyCount = 0;
        stack.clear();
        Iterator<Comm> it = this.mQueue.iterator();
        if (this.mQueue.size() > 20) {
            while (it.hasNext()) {
                stack.push(it.next());
            }
            while (!stack.empty()) {
                Comm comm = (Comm) stack.pop();
                if (comm.f13p == 30) {
                    pThirtyCount++;
                    if (pThirtyCount > 5) {
                        remove(comm);
                    }
                } else if (comm.f13p == 40) {
                    pFortyCount++;
                    if (pFortyCount > 5) {
                        remove(comm);
                    }
                } else if (comm.f13p == 50) {
                    pFiftyCount++;
                    if (pFiftyCount > 5) {
                        remove(comm);
                    }
                }
            }
        }
        this.mQueue.add(command);
        this.mFakeQueue.add(command);
    }

    private void remove(Comm comm) {
        this.mQueue.remove(comm);
        if (this.mFakeQueue.size() > this.mQueue.size()) {
            this.mFakeQueue.remove(comm);
        }
    }

    private void preempt() {
        synchronized (this.mPreemptable) {
            Iterator it = this.mPreemptable.iterator();
            while (it.hasNext()) {
                Preemptable p = (Preemptable) ((WeakReference) it.next()).get();
                if (p != null) {
                    p.preempt();
                }
            }
            this.mPreemptable.clear();
        }
    }

    public <P> void execute(AsyncTask<P, ?, ?> task, P... params) {
        this.priority = ((LoaderTask) task).mPriority;
        if (task instanceof Preemptable) {
            synchronized (this.mPreemptable) {
                this.mPreemptable.add(new WeakReference((Preemptable) task));
            }
            task.executeOnExecutor(this.mNonPreemptingExecutor, params);
            return;
        }
        task.executeOnExecutor(this, params);
    }

    public void execute(Runnable command) {
        preempt();
        if ($assertionsDisabled || command != null) {
            Comm comm = new Comm(this, null);
            comm.f13p = this.priority;
            comm.f14r = command;
            add(comm);
            return;
        }
        throw new AssertionError();
    }

    public void run() {
        while (true) {
            try {
                Runnable command = take();
                if (command != null) {
                    command.run();
                }
                this.mFakeQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
