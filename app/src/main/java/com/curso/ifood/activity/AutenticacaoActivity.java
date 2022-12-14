package com.curso.ifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.curso.ifood.R;
import com.curso.ifood.helper.ConfiguracaoFirebase;
import com.curso.ifood.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;


public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private LinearLayout linearTipoUsuario;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);
       getSupportActionBar().hide();

        inicializaComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signOut();

        //Verificar usuario logado
        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                }else{
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if( !email.isEmpty() ){
                    if( !senha.isEmpty() ){

                        //verifica estado do switch
                        if (tipoAcesso.isChecked()){ //cadastro

                            autenticacao.createUserWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Cadastro realizado com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                        String tipoUsuario = getTipoUsuario();
                                        UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);
                                        abrirTelaPrincipal(tipoUsuario);
                                    }else{
                                        String erroExcecao = "";

                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Digite um email v??lido!";
                                        }catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Esta conta j?? foi Cadastrada!";
                                        }catch (Exception e){
                                            erroExcecao = "Ao cadastrar usu??rio: " + e.getMessage();
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(AutenticacaoActivity.this,
                                                erroExcecao,
                                                Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                        }else{ //login

                            autenticacao.signInWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Logado com Sucesso",
                                                Toast.LENGTH_SHORT).show();
                                        String tipoUsuario = task.getResult().getUser().getDisplayName();
                                        abrirTelaPrincipal(tipoUsuario);

                                    }else{

                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Erro ao fazer login!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }


                    }else{
                        Toast.makeText(AutenticacaoActivity.this,
                                "Preencha o Senha!",
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AutenticacaoActivity.this,
                            "Preencha o Email!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getTipoUsuario(){

        return tipoUsuario.isChecked() ? "E" : "U";
    }

    private void verificarUsuarioLogado(){

        FirebaseUser usurioAtual = autenticacao.getCurrentUser();
        if (usurioAtual != null){
            String tipoUsuario = usurioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }

    private void abrirTelaPrincipal(String tipoUsuario){
        if (tipoUsuario.equals("E")){ //empresa
            startActivity(new Intent (getApplicationContext(), EmpresaActivity.class));
        }else{ //user
            startActivity(new Intent (getApplicationContext(), HomeActivity.class));
        }
    }


    private void inicializaComponentes(){
        campoEmail = findViewById(R.id.editEmpresaNome);
        campoSenha = findViewById(R.id.editEmpresaCategoria);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
    }

}