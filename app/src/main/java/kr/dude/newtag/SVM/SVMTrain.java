package kr.dude.newtag.SVM;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import kr.dude.newtag.libsvm.svm;
import kr.dude.newtag.libsvm.svm_model;
import kr.dude.newtag.libsvm.svm_node;
import kr.dude.newtag.libsvm.svm_parameter;
import kr.dude.newtag.libsvm.svm_problem;

/**
 * Created by madcat on 1/27/16.
 */
public class SVMTrain {

    private static final String LOG_TAG = "SVMTrain";
    private svm_parameter param;		// set by parse_command_line
    private svm_problem prob;		// set by read_problem
    private svm_model model;
    private String model_file_name;		// set by parse_command_line
    private String error_msg;
    private int cross_validation;
    private int nr_fold;


    public SVMTrain() {
        /* 파라메터 기본값 */
        svm_parameter param = new svm_parameter();

        param.svm_type = svm_parameter.ONE_CLASS;
        param.kernel_type = svm_parameter.POLY;
        param.degree = 3;
        param.gamma = 0.01041666667;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 8;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];

        cross_validation = 0;
        nr_fold = 1;

        setSVMParam(param);
    }

    public SVMTrain(svm_parameter param) {
        setSVMParam(param);
    }

    public void setSVMParam(svm_parameter param) {
        this.param = param;
    }

    public svm_parameter getSVMParam() {
        return param;
    }


    public void setModelFileName(String modelFileName) {
        model_file_name = modelFileName;
    }

    public SVMTrain loadProblem(String input_file_name) throws IOException {

        BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
            String line = fp.readLine();
            if(line == null) break;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            vy.addElement(Util.atof(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = Util.atoi(st.nextToken());
                x[j].value = Util.atof(st.nextToken());
            }
            if(m>0) max_index = Math.max(max_index, x[m-1].index);
            vx.addElement(x);
        }

        prob = new svm_problem();
        prob.l = vy.size();
        prob.x = new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y = new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;

        if(param.kernel_type == svm_parameter.PRECOMPUTED)
            for(int i=0;i<prob.l;i++)
            {
                if (prob.x[i][0].index != 0)
                {
                    LOG("Wrong kernel matrix: first column must be 0:sample_serial_number");
                    throw new RuntimeException("Wrong kernel matrix: first column must be 0:sample_serial_number");
                }
                if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
                {
                    LOG("Wrong input format: sample_serial_number out of range");
                    throw new RuntimeException("Wrong input format: sample_serial_number out of range");
                }
            }

        fp.close();


        return this;
    }


    public void doTrain() {

        LOG(" START SVM TRAINING !! " + new Date() );
//            read_problem();


        // 에러 메세지가 있다면 종료
        error_msg = svm.svm_check_parameter(prob, param);
        if (error_msg != null) {
            LOG("ERROR: " + error_msg + "\n");
            return;
        }

        try {
            if (cross_validation != 0) {
                do_cross_validation();
            } else {
                model = svm.svm_train(prob, param);

                LOG(" SVAE SVM MODEL !! " );
                svm.svm_save_model(model_file_name, model);
            }
        }
        catch(IOException e) {
            LOG(" ERROR :: " + e.getMessage());
        }

        LOG(" END SVM TRAINING !! " + new Date() );
    }


    private void do_cross_validation()
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        svm.svm_cross_validation(prob, param, nr_fold, target);
        if(param.svm_type == svm_parameter.EPSILON_SVR ||
                param.svm_type == svm_parameter.NU_SVR)
        {
            for(i=0;i<prob.l;i++)
            {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v-y)*(v-y);
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            LOG("Cross Validation Mean squared error = "+total_error/prob.l);
            LOG("Cross Validation Squared correlation coefficient = " +
                    ((prob.l * sumvy - sumv * sumy) * (prob.l * sumvy - sumv * sumy)) /
                            ((prob.l * sumvv - sumv * sumv) * (prob.l * sumyy - sumy * sumy)));
        }
        else
        {
            for(i=0;i<prob.l;i++)
                if(target[i] == prob.y[i])
                    ++total_correct;
            LOG("Cross Validation Accuracy = " + 100.0 * total_correct / prob.l);
        }
    }


    private void LOG(final String message) {
        Log.i(LOG_TAG, message);
    }
}
