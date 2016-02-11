package kr.dude.newtag.SVM;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import kr.dude.newtag.libsvm.svm;
import kr.dude.newtag.libsvm.svm_model;
import kr.dude.newtag.libsvm.svm_node;
import kr.dude.newtag.libsvm.svm_parameter;


/**
 * Created by madcat on 1/27/16.
 */
public class SVMPredict {

    private static final String LOG_TAG = "SVMPredict";
    private String modelFileName; /* SVM 모델파일 */
    private String testFileName; /* 테스트 파일 */
    private String outputFileName; /* 결과 파일 */
    private int predict_probability = 0;

    public SVMPredict(String modelFileName, String testFileName, String outputFileName) {
        this.modelFileName = modelFileName;
        this.testFileName = testFileName;
        this.outputFileName = outputFileName;
    }

    public void setPredictProbability(int predict_probability) {
        this.predict_probability = predict_probability;
    }

    public void doPredict() throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(testFileName));
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFileName)));
        svm_model model = svm.svm_load_model(modelFileName);

        if (model == null) {
            LOG("can't open model file " + modelFileName);
            throw new RuntimeException("can't open model file " + modelFileName);
        }

        if (predict_probability == 1) {
            if (svm.svm_check_probability_model(model) == 0) {
                LOG("Model does not support probabiliy estimates");
                return;
            }
        } else {
            if (svm.svm_check_probability_model(model) != 0) {
                LOG("Model supports probability estimates, but disabled in prediction.");
            }
        }



        // start predict
        predict(input,output,model,predict_probability);
        input.close();
        output.close();
    }

    private void LOG(String message) {
        Log.i(LOG_TAG, message);
    }


    private void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability)
            throws IOException {

        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
                LOG("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for (int j = 0; j < nr_class; j++)
                    output.writeBytes(" " + labels[j]);
                output.writeBytes("\n");
            }
        }
        while (true) {
            String line = input.readLine();
            if (line == null) break;

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target = Util.atof(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = Util.atoi(st.nextToken());
                x[j].value = Util.atof(st.nextToken());
            }

            double v;
            if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
                v = svm.svm_predict_probability(model, x, prob_estimates);
                output.writeBytes(v + " ");
                for (int j = 0; j < nr_class; j++)
                    output.writeBytes(prob_estimates[j] + " ");
                output.writeBytes("\n");
            } else {

                // @TODO : 예측 결과에 따른 SUM값 추출
                v = svm.svm_predict(model, x);
                output.writeBytes(v + "\n");
            }

            if (v == target)
                ++correct;
            error += (v - target) * (v - target);
            sumv += v;
            sumy += target;
            sumvv += v * v;
            sumyy += target * target;
            sumvy += v * target;
            ++total;
        }
        if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
            LOG("Mean squared error = " + error / total + " (regression)\n");
            LOG("Squared correlation coefficient = " +
                    ((total * sumvy - sumv * sumy) * (total * sumvy - sumv * sumy)) /
                            ((total * sumvv - sumv * sumv) * (total * sumyy - sumy * sumy)) +
                    " (regression)\n");
        } else
            LOG("Accuracy = " + (double) correct / total * 100 +
                    "% (" + correct + "/" + total + ") (classification)\n");
    }


}
