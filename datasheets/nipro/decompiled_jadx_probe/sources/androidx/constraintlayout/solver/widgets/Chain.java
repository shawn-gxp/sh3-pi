package androidx.constraintlayout.solver.widgets;

import androidx.constraintlayout.solver.ArrayRow;
import androidx.constraintlayout.solver.LinearSystem;
import androidx.constraintlayout.solver.SolverVariable;
import androidx.constraintlayout.solver.widgets.ConstraintWidget;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class Chain {
    private static final boolean DEBUG = false;
    public static final boolean USE_CHAIN_OPTIMIZATION = false;

    public static void applyChainConstraints(ConstraintWidgetContainer constraintWidgetContainer, LinearSystem linearSystem, ArrayList<ConstraintWidget> arrayList, int i) {
        ChainHead[] chainHeadArr;
        int i2;
        int i3;
        if (i == 0) {
            i3 = constraintWidgetContainer.mHorizontalChainsSize;
            chainHeadArr = constraintWidgetContainer.mHorizontalChainsArray;
            i2 = 0;
        } else {
            int i4 = constraintWidgetContainer.mVerticalChainsSize;
            chainHeadArr = constraintWidgetContainer.mVerticalChainsArray;
            i2 = 2;
            i3 = i4;
        }
        for (int i5 = 0; i5 < i3; i5++) {
            ChainHead chainHead = chainHeadArr[i5];
            chainHead.define();
            if (arrayList == null || (arrayList != null && arrayList.contains(chainHead.mFirst))) {
                applyChainConstraints(constraintWidgetContainer, linearSystem, i, i2, chainHead);
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x0035, code lost:
    
        if (r2.mHorizontalChainStyle == 2) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x004c, code lost:
    
        r5 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:317:0x004a, code lost:
    
        r5 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:327:0x0048, code lost:
    
        if (r2.mVerticalChainStyle == 2) goto L29;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:121:0x0269 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:137:0x04db A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:144:0x04ef  */
    /* JADX WARN: Removed duplicated region for block: B:147:0x04f8  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x04ff  */
    /* JADX WARN: Removed duplicated region for block: B:154:0x0511  */
    /* JADX WARN: Removed duplicated region for block: B:156:0x051e A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:163:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:164:0x050e  */
    /* JADX WARN: Removed duplicated region for block: B:165:0x04fb  */
    /* JADX WARN: Removed duplicated region for block: B:166:0x04f2  */
    /* JADX WARN: Removed duplicated region for block: B:172:0x02be A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:191:0x03a7  */
    /* JADX WARN: Removed duplicated region for block: B:194:0x03a8 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:239:0x03b0 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:247:0x03c3  */
    /* JADX WARN: Removed duplicated region for block: B:300:0x0493  */
    /* JADX WARN: Removed duplicated region for block: B:308:0x04c8  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x019f  */
    /* JADX WARN: Removed duplicated region for block: B:90:0x01bc  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x01d9  */
    /* JADX WARN: Type inference failed for: r2v58, types: [androidx.constraintlayout.solver.widgets.ConstraintWidget] */
    /* JADX WARN: Type inference failed for: r7v1 */
    /* JADX WARN: Type inference failed for: r7v2, types: [androidx.constraintlayout.solver.widgets.ConstraintWidget] */
    /* JADX WARN: Type inference failed for: r7v32 */
    /* JADX WARN: Type inference failed for: r7v33 */
    /* JADX WARN: Type inference failed for: r7v34 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    static void applyChainConstraints(ConstraintWidgetContainer constraintWidgetContainer, LinearSystem linearSystem, int i, int i2, ChainHead chainHead) {
        boolean z;
        boolean z2;
        boolean z3;
        ArrayList<ConstraintWidget> arrayList;
        ConstraintWidget constraintWidget;
        ConstraintAnchor constraintAnchor;
        ConstraintAnchor constraintAnchor2;
        ConstraintAnchor constraintAnchor3;
        int i3;
        ConstraintWidget constraintWidget2;
        int i4;
        ConstraintAnchor constraintAnchor4;
        SolverVariable solverVariable;
        SolverVariable solverVariable2;
        ConstraintWidget constraintWidget3;
        ConstraintAnchor constraintAnchor5;
        SolverVariable solverVariable3;
        SolverVariable solverVariable4;
        ConstraintWidget constraintWidget4;
        SolverVariable solverVariable5;
        SolverVariable solverVariable6;
        float f;
        int size;
        int i5;
        ArrayList<ConstraintWidget> arrayList2;
        int i6;
        boolean z4;
        ConstraintWidget constraintWidget5;
        boolean z5;
        int i7;
        ConstraintWidget constraintWidget6 = chainHead.mFirst;
        ConstraintWidget constraintWidget7 = chainHead.mLast;
        ConstraintWidget constraintWidget8 = chainHead.mFirstVisibleWidget;
        ConstraintWidget constraintWidget9 = chainHead.mLastVisibleWidget;
        ConstraintWidget constraintWidget10 = chainHead.mHead;
        float f2 = chainHead.mTotalWeight;
        ConstraintWidget constraintWidget11 = chainHead.mFirstMatchConstraintWidget;
        ConstraintWidget constraintWidget12 = chainHead.mLastMatchConstraintWidget;
        boolean z6 = constraintWidgetContainer.mListDimensionBehaviors[i] == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
        if (i == 0) {
            z = constraintWidget10.mHorizontalChainStyle == 0;
            z2 = constraintWidget10.mHorizontalChainStyle == 1;
        } else {
            z = constraintWidget10.mVerticalChainStyle == 0;
            z2 = constraintWidget10.mVerticalChainStyle == 1;
        }
        ?? r7 = constraintWidget6;
        boolean z7 = z2;
        boolean z8 = z;
        boolean z9 = false;
        while (true) {
            if (z9) {
                break;
            }
            ConstraintAnchor constraintAnchor6 = r7.mListAnchors[i2];
            int i8 = z3 ? 1 : 4;
            int margin = constraintAnchor6.getMargin();
            float f3 = f2;
            boolean z10 = z9;
            boolean z11 = r7.mListDimensionBehaviors[i] == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && r7.mResolvedMatchConstraintDefault[i] == 0;
            ConstraintAnchor constraintAnchor7 = constraintAnchor6.mTarget;
            if (constraintAnchor7 != null && r7 != constraintWidget6) {
                margin += constraintAnchor7.getMargin();
            }
            int i9 = margin;
            if (!z3 || r7 == constraintWidget6 || r7 == constraintWidget8) {
                z4 = z7;
            } else {
                z4 = z7;
                i8 = 8;
            }
            ConstraintAnchor constraintAnchor8 = constraintAnchor6.mTarget;
            if (constraintAnchor8 != null) {
                if (r7 == constraintWidget8) {
                    z5 = z8;
                    constraintWidget5 = constraintWidget10;
                    linearSystem.addGreaterThan(constraintAnchor6.mSolverVariable, constraintAnchor8.mSolverVariable, i9, 6);
                } else {
                    constraintWidget5 = constraintWidget10;
                    z5 = z8;
                    linearSystem.addGreaterThan(constraintAnchor6.mSolverVariable, constraintAnchor8.mSolverVariable, i9, 8);
                }
                linearSystem.addEquality(constraintAnchor6.mSolverVariable, constraintAnchor6.mTarget.mSolverVariable, i9, (!z11 || z3) ? i8 : 5);
            } else {
                constraintWidget5 = constraintWidget10;
                z5 = z8;
            }
            if (z6) {
                if (r7.getVisibility() == 8 || r7.mListDimensionBehaviors[i] != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    i7 = 0;
                } else {
                    ConstraintAnchor[] constraintAnchorArr = r7.mListAnchors;
                    i7 = 0;
                    linearSystem.addGreaterThan(constraintAnchorArr[i2 + 1].mSolverVariable, constraintAnchorArr[i2].mSolverVariable, 0, 5);
                }
                linearSystem.addGreaterThan(r7.mListAnchors[i2].mSolverVariable, constraintWidgetContainer.mListAnchors[i2].mSolverVariable, i7, 8);
            }
            ConstraintAnchor constraintAnchor9 = r7.mListAnchors[i2 + 1].mTarget;
            if (constraintAnchor9 != null) {
                ?? r2 = constraintAnchor9.mOwner;
                ConstraintAnchor[] constraintAnchorArr2 = r2.mListAnchors;
                if (constraintAnchorArr2[i2].mTarget != null && constraintAnchorArr2[i2].mTarget.mOwner == r7) {
                    r21 = r2;
                }
            }
            if (r21 != null) {
                r7 = r21;
                z9 = z10;
            } else {
                z9 = true;
            }
            z7 = z4;
            f2 = f3;
            z8 = z5;
            constraintWidget10 = constraintWidget5;
            r7 = r7;
        }
        ConstraintWidget constraintWidget13 = constraintWidget10;
        float f4 = f2;
        boolean z12 = z8;
        boolean z13 = z7;
        if (constraintWidget9 != null) {
            int i10 = i2 + 1;
            if (constraintWidget7.mListAnchors[i10].mTarget != null) {
                ConstraintAnchor constraintAnchor10 = constraintWidget9.mListAnchors[i10];
                if ((constraintWidget9.mListDimensionBehaviors[i] == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT && constraintWidget9.mResolvedMatchConstraintDefault[i] == 0) && !z3) {
                    ConstraintAnchor constraintAnchor11 = constraintAnchor10.mTarget;
                    if (constraintAnchor11.mOwner == constraintWidgetContainer) {
                        linearSystem.addEquality(constraintAnchor10.mSolverVariable, constraintAnchor11.mSolverVariable, -constraintAnchor10.getMargin(), 5);
                        linearSystem.addLowerThan(constraintAnchor10.mSolverVariable, constraintWidget7.mListAnchors[i10].mTarget.mSolverVariable, -constraintAnchor10.getMargin(), 6);
                        if (z6) {
                            int i11 = i2 + 1;
                            SolverVariable solverVariable7 = constraintWidgetContainer.mListAnchors[i11].mSolverVariable;
                            ConstraintAnchor[] constraintAnchorArr3 = constraintWidget7.mListAnchors;
                            linearSystem.addGreaterThan(solverVariable7, constraintAnchorArr3[i11].mSolverVariable, constraintAnchorArr3[i11].getMargin(), 8);
                        }
                        arrayList = chainHead.mWeightedMatchConstraintsWidgets;
                        if (arrayList != null && (size = arrayList.size()) > 1) {
                            float f5 = (chainHead.mHasUndefinedWeights || chainHead.mHasComplexMatchWeights) ? f4 : chainHead.mWidgetsMatchCount;
                            float f6 = 0.0f;
                            float f7 = 0.0f;
                            ConstraintWidget constraintWidget14 = null;
                            i5 = 0;
                            while (i5 < size) {
                                ConstraintWidget constraintWidget15 = arrayList.get(i5);
                                float f8 = constraintWidget15.mWeight[i];
                                if (f8 < f6) {
                                    if (chainHead.mHasComplexMatchWeights) {
                                        ConstraintAnchor[] constraintAnchorArr4 = constraintWidget15.mListAnchors;
                                        linearSystem.addEquality(constraintAnchorArr4[i2 + 1].mSolverVariable, constraintAnchorArr4[i2].mSolverVariable, 0, 4);
                                        arrayList2 = arrayList;
                                        i6 = size;
                                        i5++;
                                        size = i6;
                                        arrayList = arrayList2;
                                        f6 = 0.0f;
                                    } else {
                                        f8 = 1.0f;
                                        f6 = 0.0f;
                                    }
                                }
                                if (f8 == f6) {
                                    ConstraintAnchor[] constraintAnchorArr5 = constraintWidget15.mListAnchors;
                                    linearSystem.addEquality(constraintAnchorArr5[i2 + 1].mSolverVariable, constraintAnchorArr5[i2].mSolverVariable, 0, 8);
                                    arrayList2 = arrayList;
                                    i6 = size;
                                    i5++;
                                    size = i6;
                                    arrayList = arrayList2;
                                    f6 = 0.0f;
                                } else {
                                    if (constraintWidget14 != null) {
                                        ConstraintAnchor[] constraintAnchorArr6 = constraintWidget14.mListAnchors;
                                        SolverVariable solverVariable8 = constraintAnchorArr6[i2].mSolverVariable;
                                        int i12 = i2 + 1;
                                        SolverVariable solverVariable9 = constraintAnchorArr6[i12].mSolverVariable;
                                        ConstraintAnchor[] constraintAnchorArr7 = constraintWidget15.mListAnchors;
                                        arrayList2 = arrayList;
                                        SolverVariable solverVariable10 = constraintAnchorArr7[i2].mSolverVariable;
                                        SolverVariable solverVariable11 = constraintAnchorArr7[i12].mSolverVariable;
                                        i6 = size;
                                        ArrayRow createRow = linearSystem.createRow();
                                        createRow.createRowEqualMatchDimensions(f7, f5, f8, solverVariable8, solverVariable9, solverVariable10, solverVariable11);
                                        linearSystem.addConstraint(createRow);
                                    } else {
                                        arrayList2 = arrayList;
                                        i6 = size;
                                    }
                                    f7 = f8;
                                    constraintWidget14 = constraintWidget15;
                                    i5++;
                                    size = i6;
                                    arrayList = arrayList2;
                                    f6 = 0.0f;
                                }
                            }
                        }
                        if (constraintWidget8 == null && (constraintWidget8 == constraintWidget9 || z3)) {
                            ConstraintAnchor constraintAnchor12 = constraintWidget6.mListAnchors[i2];
                            int i13 = i2 + 1;
                            ConstraintAnchor constraintAnchor13 = constraintWidget7.mListAnchors[i13];
                            ConstraintAnchor constraintAnchor14 = constraintAnchor12.mTarget;
                            SolverVariable solverVariable12 = constraintAnchor14 != null ? constraintAnchor14.mSolverVariable : null;
                            ConstraintAnchor constraintAnchor15 = constraintAnchor13.mTarget;
                            SolverVariable solverVariable13 = constraintAnchor15 != null ? constraintAnchor15.mSolverVariable : null;
                            ConstraintAnchor constraintAnchor16 = constraintWidget8.mListAnchors[i2];
                            ConstraintAnchor constraintAnchor17 = constraintWidget9.mListAnchors[i13];
                            if (solverVariable12 != null && solverVariable13 != null) {
                                if (i == 0) {
                                    f = constraintWidget13.mHorizontalBiasPercent;
                                } else {
                                    f = constraintWidget13.mVerticalBiasPercent;
                                }
                                linearSystem.addCentering(constraintAnchor16.mSolverVariable, solverVariable12, constraintAnchor16.getMargin(), f, solverVariable13, constraintAnchor17.mSolverVariable, constraintAnchor17.getMargin(), 7);
                            }
                        } else if (z12 || constraintWidget8 == null) {
                            int i14 = 8;
                            if (z13 && constraintWidget8 != null) {
                                int i15 = chainHead.mWidgetsMatchCount;
                                boolean z14 = i15 <= 0 && chainHead.mWidgetsCount == i15;
                                constraintWidget = constraintWidget8;
                                ConstraintWidget constraintWidget16 = constraintWidget;
                                while (constraintWidget != null) {
                                    ConstraintWidget constraintWidget17 = constraintWidget.mNextChainWidget[i];
                                    while (constraintWidget17 != null && constraintWidget17.getVisibility() == i14) {
                                        constraintWidget17 = constraintWidget17.mNextChainWidget[i];
                                    }
                                    if (constraintWidget == constraintWidget8 || constraintWidget == constraintWidget9 || constraintWidget17 == null) {
                                        constraintWidget2 = constraintWidget16;
                                        i4 = i14;
                                    } else {
                                        ConstraintWidget constraintWidget18 = constraintWidget17 == constraintWidget9 ? null : constraintWidget17;
                                        ConstraintAnchor constraintAnchor18 = constraintWidget.mListAnchors[i2];
                                        SolverVariable solverVariable14 = constraintAnchor18.mSolverVariable;
                                        ConstraintAnchor constraintAnchor19 = constraintAnchor18.mTarget;
                                        if (constraintAnchor19 != null) {
                                            SolverVariable solverVariable15 = constraintAnchor19.mSolverVariable;
                                        }
                                        int i16 = i2 + 1;
                                        SolverVariable solverVariable16 = constraintWidget16.mListAnchors[i16].mSolverVariable;
                                        int margin2 = constraintAnchor18.getMargin();
                                        int margin3 = constraintWidget.mListAnchors[i16].getMargin();
                                        if (constraintWidget18 != null) {
                                            constraintAnchor4 = constraintWidget18.mListAnchors[i2];
                                            solverVariable = constraintAnchor4.mSolverVariable;
                                            ConstraintAnchor constraintAnchor20 = constraintAnchor4.mTarget;
                                            solverVariable2 = constraintAnchor20 != null ? constraintAnchor20.mSolverVariable : null;
                                        } else {
                                            constraintAnchor4 = constraintWidget9.mListAnchors[i2];
                                            solverVariable = constraintAnchor4 != null ? constraintAnchor4.mSolverVariable : null;
                                            solverVariable2 = constraintWidget.mListAnchors[i16].mSolverVariable;
                                        }
                                        if (constraintAnchor4 != null) {
                                            margin3 += constraintAnchor4.getMargin();
                                        }
                                        int i17 = margin3;
                                        if (constraintWidget16 != null) {
                                            margin2 += constraintWidget16.mListAnchors[i16].getMargin();
                                        }
                                        int i18 = margin2;
                                        int i19 = z14 ? 8 : 4;
                                        if (solverVariable14 == null || solverVariable16 == null || solverVariable == null || solverVariable2 == null) {
                                            constraintWidget3 = constraintWidget18;
                                            constraintWidget2 = constraintWidget16;
                                            i4 = 8;
                                        } else {
                                            constraintWidget3 = constraintWidget18;
                                            constraintWidget2 = constraintWidget16;
                                            i4 = 8;
                                            linearSystem.addCentering(solverVariable14, solverVariable16, i18, 0.5f, solverVariable, solverVariable2, i17, i19);
                                        }
                                        constraintWidget17 = constraintWidget3;
                                    }
                                    if (constraintWidget.getVisibility() == i4) {
                                        constraintWidget = constraintWidget2;
                                    }
                                    i14 = i4;
                                    constraintWidget16 = constraintWidget;
                                    constraintWidget = constraintWidget17;
                                }
                                ConstraintAnchor constraintAnchor21 = constraintWidget8.mListAnchors[i2];
                                constraintAnchor = constraintWidget6.mListAnchors[i2].mTarget;
                                int i20 = i2 + 1;
                                constraintAnchor2 = constraintWidget9.mListAnchors[i20];
                                constraintAnchor3 = constraintWidget7.mListAnchors[i20].mTarget;
                                if (constraintAnchor != null) {
                                    i3 = 5;
                                } else if (constraintWidget8 != constraintWidget9) {
                                    i3 = 5;
                                    linearSystem.addEquality(constraintAnchor21.mSolverVariable, constraintAnchor.mSolverVariable, constraintAnchor21.getMargin(), 5);
                                } else {
                                    i3 = 5;
                                    if (constraintAnchor3 != null) {
                                        linearSystem.addCentering(constraintAnchor21.mSolverVariable, constraintAnchor.mSolverVariable, constraintAnchor21.getMargin(), 0.5f, constraintAnchor2.mSolverVariable, constraintAnchor3.mSolverVariable, constraintAnchor2.getMargin(), 5);
                                    }
                                }
                                if (constraintAnchor3 != null && constraintWidget8 != constraintWidget9) {
                                    linearSystem.addEquality(constraintAnchor2.mSolverVariable, constraintAnchor3.mSolverVariable, -constraintAnchor2.getMargin(), i3);
                                }
                            }
                        } else {
                            int i21 = chainHead.mWidgetsMatchCount;
                            boolean z15 = i21 > 0 && chainHead.mWidgetsCount == i21;
                            ConstraintWidget constraintWidget19 = constraintWidget8;
                            ConstraintWidget constraintWidget20 = constraintWidget19;
                            while (constraintWidget19 != null) {
                                ConstraintWidget constraintWidget21 = constraintWidget19.mNextChainWidget[i];
                                while (constraintWidget21 != null && constraintWidget21.getVisibility() == 8) {
                                    constraintWidget21 = constraintWidget21.mNextChainWidget[i];
                                }
                                if (constraintWidget21 != null || constraintWidget19 == constraintWidget9) {
                                    ConstraintAnchor constraintAnchor22 = constraintWidget19.mListAnchors[i2];
                                    SolverVariable solverVariable17 = constraintAnchor22.mSolverVariable;
                                    ConstraintAnchor constraintAnchor23 = constraintAnchor22.mTarget;
                                    SolverVariable solverVariable18 = constraintAnchor23 != null ? constraintAnchor23.mSolverVariable : null;
                                    if (constraintWidget20 != constraintWidget19) {
                                        solverVariable18 = constraintWidget20.mListAnchors[i2 + 1].mSolverVariable;
                                    } else if (constraintWidget19 == constraintWidget8 && constraintWidget20 == constraintWidget19) {
                                        ConstraintAnchor[] constraintAnchorArr8 = constraintWidget6.mListAnchors;
                                        solverVariable18 = constraintAnchorArr8[i2].mTarget != null ? constraintAnchorArr8[i2].mTarget.mSolverVariable : null;
                                    }
                                    int margin4 = constraintAnchor22.getMargin();
                                    int i22 = i2 + 1;
                                    int margin5 = constraintWidget19.mListAnchors[i22].getMargin();
                                    if (constraintWidget21 != null) {
                                        constraintAnchor5 = constraintWidget21.mListAnchors[i2];
                                        SolverVariable solverVariable19 = constraintAnchor5.mSolverVariable;
                                        solverVariable4 = constraintWidget19.mListAnchors[i22].mSolverVariable;
                                        solverVariable3 = solverVariable19;
                                    } else {
                                        constraintAnchor5 = constraintWidget7.mListAnchors[i22].mTarget;
                                        solverVariable3 = constraintAnchor5 != null ? constraintAnchor5.mSolverVariable : null;
                                        solverVariable4 = constraintWidget19.mListAnchors[i22].mSolverVariable;
                                    }
                                    if (constraintAnchor5 != null) {
                                        margin5 += constraintAnchor5.getMargin();
                                    }
                                    if (constraintWidget20 != null) {
                                        margin4 += constraintWidget20.mListAnchors[i22].getMargin();
                                    }
                                    if (solverVariable17 != null && solverVariable18 != null && solverVariable3 != null && solverVariable4 != null) {
                                        if (constraintWidget19 == constraintWidget8) {
                                            margin4 = constraintWidget8.mListAnchors[i2].getMargin();
                                        }
                                        int i23 = margin4;
                                        constraintWidget4 = constraintWidget21;
                                        linearSystem.addCentering(solverVariable17, solverVariable18, i23, 0.5f, solverVariable3, solverVariable4, constraintWidget19 == constraintWidget9 ? constraintWidget9.mListAnchors[i22].getMargin() : margin5, z15 ? 8 : 5);
                                        if (constraintWidget19.getVisibility() == 8) {
                                            constraintWidget20 = constraintWidget19;
                                        }
                                        constraintWidget19 = constraintWidget4;
                                    }
                                }
                                constraintWidget4 = constraintWidget21;
                                if (constraintWidget19.getVisibility() == 8) {
                                }
                                constraintWidget19 = constraintWidget4;
                            }
                        }
                        if ((z12 && !z13) || constraintWidget8 == null || constraintWidget8 == constraintWidget9) {
                            return;
                        }
                        ConstraintAnchor constraintAnchor24 = constraintWidget8.mListAnchors[i2];
                        int i24 = i2 + 1;
                        ConstraintAnchor constraintAnchor25 = constraintWidget9.mListAnchors[i24];
                        ConstraintAnchor constraintAnchor26 = constraintAnchor24.mTarget;
                        solverVariable5 = constraintAnchor26 != null ? constraintAnchor26.mSolverVariable : null;
                        ConstraintAnchor constraintAnchor27 = constraintAnchor25.mTarget;
                        SolverVariable solverVariable20 = constraintAnchor27 != null ? constraintAnchor27.mSolverVariable : null;
                        if (constraintWidget7 != constraintWidget9) {
                            ConstraintAnchor constraintAnchor28 = constraintWidget7.mListAnchors[i24].mTarget;
                            solverVariable6 = constraintAnchor28 != null ? constraintAnchor28.mSolverVariable : null;
                        } else {
                            solverVariable6 = solverVariable20;
                        }
                        if (constraintWidget8 == constraintWidget9) {
                            ConstraintAnchor[] constraintAnchorArr9 = constraintWidget8.mListAnchors;
                            ConstraintAnchor constraintAnchor29 = constraintAnchorArr9[i2];
                            constraintAnchor25 = constraintAnchorArr9[i24];
                            constraintAnchor24 = constraintAnchor29;
                        }
                        if (solverVariable5 == null || solverVariable6 == null) {
                            return;
                        }
                        int margin6 = constraintAnchor24.getMargin();
                        if (constraintWidget9 != null) {
                            constraintWidget7 = constraintWidget9;
                        }
                        linearSystem.addCentering(constraintAnchor24.mSolverVariable, solverVariable5, margin6, 0.5f, solverVariable6, constraintAnchor25.mSolverVariable, constraintWidget7.mListAnchors[i24].getMargin(), 5);
                        return;
                    }
                }
                if (z3) {
                    ConstraintAnchor constraintAnchor30 = constraintAnchor10.mTarget;
                    if (constraintAnchor30.mOwner == constraintWidgetContainer) {
                        linearSystem.addEquality(constraintAnchor10.mSolverVariable, constraintAnchor30.mSolverVariable, -constraintAnchor10.getMargin(), 4);
                    }
                }
                linearSystem.addLowerThan(constraintAnchor10.mSolverVariable, constraintWidget7.mListAnchors[i10].mTarget.mSolverVariable, -constraintAnchor10.getMargin(), 6);
                if (z6) {
                }
                arrayList = chainHead.mWeightedMatchConstraintsWidgets;
                if (arrayList != null) {
                    if (chainHead.mHasUndefinedWeights) {
                    }
                    float f62 = 0.0f;
                    float f72 = 0.0f;
                    ConstraintWidget constraintWidget142 = null;
                    i5 = 0;
                    while (i5 < size) {
                    }
                }
                if (constraintWidget8 == null) {
                }
                if (z12) {
                }
                int i142 = 8;
                if (z13) {
                    int i152 = chainHead.mWidgetsMatchCount;
                    if (i152 <= 0) {
                    }
                    constraintWidget = constraintWidget8;
                    ConstraintWidget constraintWidget162 = constraintWidget;
                    while (constraintWidget != null) {
                    }
                    ConstraintAnchor constraintAnchor212 = constraintWidget8.mListAnchors[i2];
                    constraintAnchor = constraintWidget6.mListAnchors[i2].mTarget;
                    int i202 = i2 + 1;
                    constraintAnchor2 = constraintWidget9.mListAnchors[i202];
                    constraintAnchor3 = constraintWidget7.mListAnchors[i202].mTarget;
                    if (constraintAnchor != null) {
                    }
                    if (constraintAnchor3 != null) {
                        linearSystem.addEquality(constraintAnchor2.mSolverVariable, constraintAnchor3.mSolverVariable, -constraintAnchor2.getMargin(), i3);
                    }
                }
                if (z12) {
                }
                ConstraintAnchor constraintAnchor242 = constraintWidget8.mListAnchors[i2];
                int i242 = i2 + 1;
                ConstraintAnchor constraintAnchor252 = constraintWidget9.mListAnchors[i242];
                ConstraintAnchor constraintAnchor262 = constraintAnchor242.mTarget;
                if (constraintAnchor262 != null) {
                }
                ConstraintAnchor constraintAnchor272 = constraintAnchor252.mTarget;
                if (constraintAnchor272 != null) {
                }
                if (constraintWidget7 != constraintWidget9) {
                }
                if (constraintWidget8 == constraintWidget9) {
                }
                if (solverVariable5 == null) {
                    return;
                } else {
                    return;
                }
            }
        }
        if (z6) {
        }
        arrayList = chainHead.mWeightedMatchConstraintsWidgets;
        if (arrayList != null) {
        }
        if (constraintWidget8 == null) {
        }
        if (z12) {
        }
        int i1422 = 8;
        if (z13) {
        }
        if (z12) {
        }
        ConstraintAnchor constraintAnchor2422 = constraintWidget8.mListAnchors[i2];
        int i2422 = i2 + 1;
        ConstraintAnchor constraintAnchor2522 = constraintWidget9.mListAnchors[i2422];
        ConstraintAnchor constraintAnchor2622 = constraintAnchor2422.mTarget;
        if (constraintAnchor2622 != null) {
        }
        ConstraintAnchor constraintAnchor2722 = constraintAnchor2522.mTarget;
        if (constraintAnchor2722 != null) {
        }
        if (constraintWidget7 != constraintWidget9) {
        }
        if (constraintWidget8 == constraintWidget9) {
        }
        if (solverVariable5 == null) {
        }
    }
}
