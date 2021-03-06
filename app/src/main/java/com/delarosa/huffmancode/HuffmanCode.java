package com.delarosa.huffmancode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class HuffmanCode extends AppCompatActivity {
    private static double entropy;
    private static int total;
    private static List<String> freqArray = new ArrayList<>();
    private static List<String> probArray = new ArrayList<>();
    private static List<HuffmanDto> huffmanCode = new ArrayList<>();
    private static TextView entropyTextView, cabecera, codigoHuffmanText;
    private static EditText textToView;
    private static boolean refresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setea la vista
        setContentView(R.layout.huffman_code);
        //valido si el intent tiene datos
        String text;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            text = bundle.getString("text");
        } else {
            text = "ejemplo clase teoria de la informacion";
        }

        entropyTextView = findViewById(R.id.entropy_result);
        cabecera = findViewById(R.id.cabecera);
        codigoHuffmanText = findViewById(R.id.codigo_huffman_text);
        textToView = findViewById(R.id.text_to_view);
        textToView.setText(text);
        //escuchador al texto.... cada vez que cambie ejecuta el metodo evevaluateCodeHuffmanalu
        textToView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh = true;
                entropy = 0;
                total = 0;
                freqArray = new ArrayList<>();
                probArray = new ArrayList<>();

                evaluateCodeHuffman(evalText(String.valueOf(textToView.getText())));
                if (s.length() == 0) {
                    codigoHuffmanText.setText("");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

        });

        //evalua el codigo huffman
        evaluateCodeHuffman(evalText(text));

    }

    /**
     * este metodo evalua las tildes
     *
     * @param text
     * @return
     */
    private String evalText(String text) {
        text = (text.contains("á")) ? text.replace("á", "a") : text;
        text = (text.contains("é")) ? text.replace("é", "e") : text;
        text = (text.contains("í")) ? text.replace("í", "i") : text;
        text = (text.contains("ó")) ? text.replace("ó", "o") : text;
        text = (text.contains("ú")) ? text.replace("ú", "u") : text;
        return text;

    }

    /**
     * evalua el codigo huffman
     *
     * @param text
     */
    private void evaluateCodeHuffman(String text) {
        try {
            //array que mide la frecuencia de cada simbolo
            int[] frecuenciaList = new int[258];
            //lee cada caracter del texto y lo asigna al array
            for (char c : text.toCharArray())
                frecuenciaList[c]++;

            frequencyTree tree = buildTree(frecuenciaList);
            cabecera.setText("SIMBOLO\t FRECUENCIA\tCODIGO HUFFMAN");
            printCodes(tree, new StringBuffer());
            for (String frecuency : freqArray) {
                probArray.add(Double.toString(Double.parseDouble(frecuency) / total));
            }
            for (String prob : probArray) {
                entropy += Double.parseDouble(prob) * Math.log10(1 / Double.parseDouble(prob)) / Math.log10(2);
            }
            if (text.equals(""))
                entropy = 0;
            entropyTextView.setText("entropia: " + String.valueOf(entropy) + " bit/simbolo");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // array de frecuencias
    public static frequencyTree buildTree(int[] charFreqs) {
        PriorityQueue<frequencyTree> trees = new PriorityQueue<frequencyTree>();
        // inicialmnente tenemos muchas hojas
        // uno para cada elemento vacio
        for (int i = 0; i < charFreqs.length; i++)
            if (charFreqs[i] > 0)
                trees.offer(new frequencyLeaf(charFreqs[i], (char) i));

        assert trees.size() > 0;
        // bucle hasta que solo quede un árbol
        while (trees.size() > 1) {
            // dos arboles con la menor frecuencia
            frequencyTree a = trees.poll();
            frequencyTree b = trees.poll();

            // ponemos en un nuevo nodo y volver a insertar en la cola
            trees.offer(new frequencyNode(a, b));
        }
        return trees.poll();
    }

    /**
     * este metodo se encarga de crear los prefijos para cada simbolo y imprime el codigo huffman
     * cuando entra por primera vez el prefijo es vacio
     *
     * @param tree
     * @param prefix
     */
    public static void printCodes(frequencyTree tree, StringBuffer prefix) {
        assert tree != null;
        if (tree instanceof frequencyLeaf) {
            frequencyLeaf leaf = (frequencyLeaf) tree;

            if (prefix.length() == 0) {
                prefix.append('0');
            }

            //justificamos el texto con espacios (25)
            String cadena = String.format("%s %25s  %25s  ", leaf.value, leaf.frequency, prefix);

            //si el texto queda vacio
            if (refresh) {
                codigoHuffmanText.setText("");
                huffmanCode = new ArrayList<>();
            }

        //region agregamos el simbolo y su codigo correspondiente para una posible decodificacion
            HuffmanDto huffmanDto = new HuffmanDto();
            huffmanDto.setSymbol(String.valueOf(leaf.value));
            huffmanDto.setCode(String.valueOf(prefix));
            huffmanCode.add(huffmanDto);
            //endregion

            //mostramos el resultado
            codigoHuffmanText.setText(String.valueOf(codigoHuffmanText.getText()) + "\n" + cadena);
            refresh = false;

            //para el calculo de la entropia
            total += leaf.frequency;
            freqArray.add(Integer.toString(leaf.frequency));


        } else if (tree instanceof frequencyNode) {
            frequencyNode node = (frequencyNode) tree;

            //crea el prefijo 0, vuelve a llamar al metodo.
            prefix.append('0');
            printCodes(node.left, prefix);
            prefix.deleteCharAt(prefix.length() - 1);

            //crea el prefijo 1, vuelve a llamar al metodo.
            prefix.append('1');
            printCodes(node.right, prefix);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /**
     * este metodo me lleva a la clase que decodifica el huffman
     * @param v
     */
    public void decode(View v) {
        Intent intent = new Intent(HuffmanCode.this, DecodeHuffman.class);
        intent.putExtra("huffmanCodeList", (Serializable) huffmanCode);
        startActivity(intent);
    }


}
