package android.support.p000v4.app;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@TargetApi(21)
@RequiresApi(21)
/* renamed from: android.support.v4.app.FragmentTransitionCompat21 */
class FragmentTransitionCompat21 {
    FragmentTransitionCompat21() {
    }

    public static Object cloneTransition(Object transition) {
        if (transition != null) {
            return ((Transition) transition).clone();
        }
        return null;
    }

    public static Object wrapTransitionInSet(Object transition) {
        if (transition == null) {
            return null;
        }
        Object transitionSet = new TransitionSet();
        transitionSet.addTransition((Transition) transition);
        return transitionSet;
    }

    public static void setSharedElementTargets(Object transitionObj, View nonExistentView, ArrayList<View> sharedViews) {
        TransitionSet transition = (TransitionSet) transitionObj;
        List<View> views = transition.getTargets();
        views.clear();
        int count = sharedViews.size();
        for (int i = 0; i < count; i++) {
            FragmentTransitionCompat21.bfsAddViewChildren(views, (View) sharedViews.get(i));
        }
        views.add(nonExistentView);
        sharedViews.add(nonExistentView);
        FragmentTransitionCompat21.addTargets(transition, sharedViews);
    }

    private static void bfsAddViewChildren(List<View> views, View startView) {
        int startIndex = views.size();
        if (!FragmentTransitionCompat21.containedBeforeIndex(views, startView, startIndex)) {
            views.add(startView);
            for (int index = startIndex; index < views.size(); index++) {
                View view = (View) views.get(index);
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int childCount = viewGroup.getChildCount();
                    for (int childIndex = 0; childIndex < childCount; childIndex++) {
                        View child = viewGroup.getChildAt(childIndex);
                        if (!FragmentTransitionCompat21.containedBeforeIndex(views, child, startIndex)) {
                            views.add(child);
                        }
                    }
                }
            }
        }
    }

    private static boolean containedBeforeIndex(List<View> views, View view, int maxIndex) {
        for (int i = 0; i < maxIndex; i++) {
            if (views.get(i) == view) {
                return true;
            }
        }
        return false;
    }

    public static void setEpicenter(Object transitionObj, View view) {
        if (view != null) {
            Transition transition = (Transition) transitionObj;
            final Rect epicenter = new Rect();
            FragmentTransitionCompat21.getBoundsOnScreen(view, epicenter);
            transition.setEpicenterCallback(new EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    return epicenter;
                }
            });
        }
    }

    public static void getBoundsOnScreen(View view, Rect epicenter) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        epicenter.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
    }

    public static void addTargets(Object transitionObj, ArrayList<View> views) {
        Transition transition = (Transition) transitionObj;
        if (transition != null) {
            int i;
            if (transition instanceof TransitionSet) {
                TransitionSet set = (TransitionSet) transition;
                int numTransitions = set.getTransitionCount();
                for (i = 0; i < numTransitions; i++) {
                    FragmentTransitionCompat21.addTargets(set.getTransitionAt(i), views);
                }
            } else if (!FragmentTransitionCompat21.hasSimpleTarget(transition) && FragmentTransitionCompat21.isNullOrEmpty(transition.getTargets())) {
                int numViews = views.size();
                for (i = 0; i < numViews; i++) {
                    transition.addTarget((View) views.get(i));
                }
            }
        }
    }

    private static boolean hasSimpleTarget(Transition transition) {
        return (FragmentTransitionCompat21.isNullOrEmpty(transition.getTargetIds()) && FragmentTransitionCompat21.isNullOrEmpty(transition.getTargetNames()) && FragmentTransitionCompat21.isNullOrEmpty(transition.getTargetTypes())) ? false : true;
    }

    private static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static Object mergeTransitionsTogether(Object transition1, Object transition2, Object transition3) {
        TransitionSet transitionSet = new TransitionSet();
        if (transition1 != null) {
            transitionSet.addTransition((Transition) transition1);
        }
        if (transition2 != null) {
            transitionSet.addTransition((Transition) transition2);
        }
        if (transition3 != null) {
            transitionSet.addTransition((Transition) transition3);
        }
        return transitionSet;
    }

    public static void scheduleHideFragmentView(Object exitTransitionObj, final View fragmentView, final ArrayList<View> exitingViews) {
        ((Transition) exitTransitionObj).addListener(new TransitionListener() {
            public void onTransitionStart(Transition transition) {
            }

            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                fragmentView.setVisibility(8);
                int numViews = exitingViews.size();
                for (int i = 0; i < numViews; i++) {
                    ((View) exitingViews.get(i)).setVisibility(0);
                }
            }

            public void onTransitionCancel(Transition transition) {
            }

            public void onTransitionPause(Transition transition) {
            }

            public void onTransitionResume(Transition transition) {
            }
        });
    }

    public static Object mergeTransitionsInSequence(Object exitTransitionObj, Object enterTransitionObj, Object sharedElementTransitionObj) {
        Transition staggered = null;
        Transition exitTransition = (Transition) exitTransitionObj;
        Transition enterTransition = (Transition) enterTransitionObj;
        Transition sharedElementTransition = (Transition) sharedElementTransitionObj;
        if (exitTransition != null && enterTransition != null) {
            staggered = new TransitionSet().addTransition(exitTransition).addTransition(enterTransition).setOrdering(1);
        } else if (exitTransition != null) {
            staggered = exitTransition;
        } else if (enterTransition != null) {
            staggered = enterTransition;
        }
        if (sharedElementTransition == null) {
            return staggered;
        }
        TransitionSet together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Object transition) {
        TransitionManager.beginDelayedTransition(sceneRoot, (Transition) transition);
    }

    public static ArrayList<String> prepareSetNameOverridesOptimized(ArrayList<View> sharedElementsIn) {
        ArrayList<String> names = new ArrayList();
        int numSharedElements = sharedElementsIn.size();
        for (int i = 0; i < numSharedElements; i++) {
            View view = (View) sharedElementsIn.get(i);
            names.add(view.getTransitionName());
            view.setTransitionName(null);
        }
        return names;
    }

    public static void setNameOverridesOptimized(View sceneRoot, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, ArrayList<String> inNames, Map<String, String> nameOverrides) {
        final int numSharedElements = sharedElementsIn.size();
        final ArrayList<String> outNames = new ArrayList();
        for (int i = 0; i < numSharedElements; i++) {
            View view = (View) sharedElementsOut.get(i);
            String name = view.getTransitionName();
            outNames.add(name);
            if (name != null) {
                view.setTransitionName(null);
                String inName = (String) nameOverrides.get(name);
                for (int j = 0; j < numSharedElements; j++) {
                    if (inName.equals(inNames.get(j))) {
                        ((View) sharedElementsIn.get(j)).setTransitionName(name);
                        break;
                    }
                }
            }
        }
        final View view2 = sceneRoot;
        final ArrayList<View> arrayList = sharedElementsIn;
        final ArrayList<String> arrayList2 = inNames;
        final ArrayList<View> arrayList3 = sharedElementsOut;
        sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                view2.getViewTreeObserver().removeOnPreDrawListener(this);
                for (int i = 0; i < numSharedElements; i++) {
                    ((View) arrayList.get(i)).setTransitionName((String) arrayList2.get(i));
                    ((View) arrayList3.get(i)).setTransitionName((String) outNames.get(i));
                }
                return true;
            }
        });
    }

    public static void captureTransitioningViews(ArrayList<View> transitioningViews, View view) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.isTransitionGroup()) {
                transitioningViews.add(viewGroup);
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                FragmentTransitionCompat21.captureTransitioningViews(transitioningViews, viewGroup.getChildAt(i));
            }
            return;
        }
        transitioningViews.add(view);
    }

    public static void findNamedViews(Map<String, View> namedViews, View view) {
        if (view.getVisibility() == 0) {
            String transitionName = view.getTransitionName();
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    FragmentTransitionCompat21.findNamedViews(namedViews, viewGroup.getChildAt(i));
                }
            }
        }
    }

    public static void setNameOverridesUnoptimized(final View sceneRoot, final ArrayList<View> sharedElementsIn, final Map<String, String> nameOverrides) {
        sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                int numSharedElements = sharedElementsIn.size();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = (View) sharedElementsIn.get(i);
                    String name = view.getTransitionName();
                    if (name != null) {
                        view.setTransitionName(FragmentTransitionCompat21.findKeyForValue(nameOverrides, name));
                    }
                }
                return true;
            }
        });
    }

    private static String findKeyForValue(Map<String, String> map, String value) {
        for (Entry<String, String> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    public static void scheduleRemoveTargets(Object overallTransitionObj, Object enterTransition, ArrayList<View> enteringViews, Object exitTransition, ArrayList<View> exitingViews, Object sharedElementTransition, ArrayList<View> sharedElementsIn) {
        final Object obj = enterTransition;
        final ArrayList<View> arrayList = enteringViews;
        final Object obj2 = exitTransition;
        final ArrayList<View> arrayList2 = exitingViews;
        final Object obj3 = sharedElementTransition;
        final ArrayList<View> arrayList3 = sharedElementsIn;
        ((Transition) overallTransitionObj).addListener(new TransitionListener() {
            public void onTransitionStart(Transition transition) {
                if (obj != null) {
                    FragmentTransitionCompat21.replaceTargets(obj, arrayList, null);
                }
                if (obj2 != null) {
                    FragmentTransitionCompat21.replaceTargets(obj2, arrayList2, null);
                }
                if (obj3 != null) {
                    FragmentTransitionCompat21.replaceTargets(obj3, arrayList3, null);
                }
            }

            public void onTransitionEnd(Transition transition) {
            }

            public void onTransitionCancel(Transition transition) {
            }

            public void onTransitionPause(Transition transition) {
            }

            public void onTransitionResume(Transition transition) {
            }
        });
    }

    public static void swapSharedElementTargets(Object sharedElementTransitionObj, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn) {
        TransitionSet sharedElementTransition = (TransitionSet) sharedElementTransitionObj;
        if (sharedElementTransition != null) {
            sharedElementTransition.getTargets().clear();
            sharedElementTransition.getTargets().addAll(sharedElementsIn);
            FragmentTransitionCompat21.replaceTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
        }
    }

    public static void replaceTargets(Object transitionObj, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        Transition transition = (Transition) transitionObj;
        int i;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (i = 0; i < numTransitions; i++) {
                FragmentTransitionCompat21.replaceTargets(set.getTransitionAt(i), oldTargets, newTargets);
            }
        } else if (!FragmentTransitionCompat21.hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
                int targetCount = newTargets == null ? 0 : newTargets.size();
                for (i = 0; i < targetCount; i++) {
                    transition.addTarget((View) newTargets.get(i));
                }
                for (i = oldTargets.size() - 1; i >= 0; i--) {
                    transition.removeTarget((View) oldTargets.get(i));
                }
            }
        }
    }

    public static void addTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).addTarget(view);
        }
    }

    public static void removeTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).removeTarget(view);
        }
    }

    public static void setEpicenter(Object transitionObj, final Rect epicenter) {
        if (transitionObj != null) {
            ((Transition) transitionObj).setEpicenterCallback(new EpicenterCallback() {
                public Rect onGetEpicenter(Transition transition) {
                    if (epicenter == null || epicenter.isEmpty()) {
                        return null;
                    }
                    return epicenter;
                }
            });
        }
    }

    public static void scheduleNameReset(final ViewGroup sceneRoot, final ArrayList<View> sharedElementsIn, final Map<String, String> nameOverrides) {
        sceneRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                sceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                int numSharedElements = sharedElementsIn.size();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = (View) sharedElementsIn.get(i);
                    view.setTransitionName((String) nameOverrides.get(view.getTransitionName()));
                }
                return true;
            }
        });
    }
}
