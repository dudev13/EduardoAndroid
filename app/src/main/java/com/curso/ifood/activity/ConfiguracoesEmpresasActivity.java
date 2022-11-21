package com.curso.ifood.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.curso.ifood.R;
import com.curso.ifood.helper.ConfiguracaoFirebase;
import com.curso.ifood.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ConfiguracoesEmpresasActivity extends AppCompatActivity {

    private EditText editEmpresaNome, editEmpresaCategoria,
            editEmpresaTempo, editEmpresaTaxa;
    private ImageView imagePerfilEmpresa;

    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";

    ActivityResultLauncher<String> mTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_empresas);
        //configuracoes Inicais
        inicializarComponentes();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        idUsuarioLogado = UsuarioFirebase.getIdUsuarios();

        //configuracoes toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //pick image


        imagePerfilEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );
                if (i.resolveActivity(getPackageManager()) != null) {
                    mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {

                            imagePerfilEmpresa.setImageURI(result);

                        }
                    });
                }
            }
        });
    }

    public void validarDadosEmpresa() {
        //valida campos preenchidos
        String nome = editEmpresaNome.getText().toString();
        String taxa = editEmpresaTaxa.getText().toString();
        String categoria = editEmpresaCategoria.getText().toString();
        String tempo = editEmpresaTempo.getText().toString();
    }


    @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);


            if (resultCode == RESULT_OK) {
                Bitmap imagem = null;

                try {
                    switch (requestCode) {
                        case SELECAO_GALERIA:
                            Uri localImagem = data.getData();
                            imagem = MediaStore.Images
                                    .Media
                                    .getBitmap(
                                            getContentResolver(),
                                            localImagem
                                    );
                            break;
                    }

                    if (imagem != null) {

                        imagePerfilEmpresa.setImageBitmap(imagem);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] dadosImagem = baos.toByteArray();

                        final StorageReference imagemRef = storageReference
                                .child("imagens")
                                .child("Empresas")
                                .child(idUsuarioLogado + "JPEG");

                        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ConfiguracoesEmpresasActivity.this,
                                        "Erro ao fazer upload da imagem",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                urlImagemSelecionada = String.valueOf(imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        Uri url = task.getResult();
                                    }
                                }));
                                Toast.makeText(ConfiguracoesEmpresasActivity.this,
                                        "Sucesso ao fazer upload da imagem",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void inicializarComponentes () {
            editEmpresaNome = findViewById(R.id.editEmpresaNome);
            editEmpresaCategoria = findViewById(R.id.editEmpresaCategoria);
            editEmpresaTempo = findViewById(R.id.editEmpresaTempo);
            editEmpresaTaxa = findViewById(R.id.editEmpresaTaxa);
            imagePerfilEmpresa = findViewById(R.id.imagePerfilEmpresa);
        }
    }



