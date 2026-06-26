package com.example.loginsistema;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Login
    private MaterialCardView cardLogin;
    private MaterialCardView cardCadastro;
    private EditText etNomeUsuarioLogin;
    private EditText etSenhaLogin;
    private MaterialButton btnEntrar;

    // Cadastro
    private TextView tvSaudacao;
    private EditText etNomeCompleto;
    private EditText etCpf;
    private EditText etRua;
    private EditText etNumeroRua;
    private EditText etBairro;
    private EditText etCep;
    private RadioGroup rgGenero;
    private DatePicker dpDataNascimento;
    private MaterialButton btnCadastrar;
    private MaterialButton btnLimpar;
    private MaterialButton btnSair;
    private TextView tvResumoCadastro;

    // Busca e lista
    private EditText etBuscaCadastro;
    private MaterialButton btnBuscarCadastro;
    private MaterialButton btnLimparBusca;
    private RecyclerView rvCadastros;
    private TextView tvSemResultados;

    // Layouts para erro amigável nos campos
    private TextInputLayout tilCpf;
    private TextInputLayout tilCep;

    // Dados de login local (sem API/banco remoto)
    private final List<Usuario> listaUsuarios = new ArrayList<>();

    // Banco local com os cadastros da tela
    private PessoaDatabaseHelper databaseHelper;
    private PessoaAdapter pessoaAdapter;

    // Controla se estamos criando um novo cadastro ou editando um existente.
    private long idPessoaEmEdicao = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarViews();
        cadastrarUsuariosLogin();

        databaseHelper = new PessoaDatabaseHelper(this);
        databaseHelper.inserirDadosFicticiosSeNecessario();

        configurarDatePicker();
        configurarRecyclerView();
        configurarMascaras();
        configurarAcoes();

        tvResumoCadastro.setText("Nenhum cadastro realizado nesta sessão.");
        atualizarModoCadastro(false, null);
        carregarListaCadastros("");
    }

    private void inicializarViews() {
        cardLogin = findViewById(R.id.cardLogin);
        cardCadastro = findViewById(R.id.cardCadastro);
        etNomeUsuarioLogin = findViewById(R.id.etNomeUsuarioLogin);
        etSenhaLogin = findViewById(R.id.etSenhaLogin);
        btnEntrar = findViewById(R.id.btnEntrar);

        tvSaudacao = findViewById(R.id.tvSaudacao);
        etNomeCompleto = findViewById(R.id.etNomeCompleto);
        etCpf = findViewById(R.id.etCpf);
        etRua = findViewById(R.id.etRua);
        etNumeroRua = findViewById(R.id.etNumeroRua);
        etBairro = findViewById(R.id.etBairro);
        etCep = findViewById(R.id.etCep);
        rgGenero = findViewById(R.id.rgGenero);
        dpDataNascimento = findViewById(R.id.dpDataNascimento);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnSair = findViewById(R.id.btnSair);
        tvResumoCadastro = findViewById(R.id.tvResumoCadastro);

        etBuscaCadastro = findViewById(R.id.etBuscaCadastro);
        btnBuscarCadastro = findViewById(R.id.btnBuscarCadastro);
        btnLimparBusca = findViewById(R.id.btnLimparBusca);
        rvCadastros = findViewById(R.id.rvCadastros);
        tvSemResultados = findViewById(R.id.tvSemResultados);

        tilCpf = findViewById(R.id.tilCpf);
        tilCep = findViewById(R.id.tilCep);
    }

    private void configurarDatePicker() {
        dpDataNascimento.setMaxDate(System.currentTimeMillis());
        dpDataNascimento.updateDate(2000, 0, 1);
    }

    private void configurarRecyclerView() {
        pessoaAdapter = new PessoaAdapter(new PessoaAdapter.OnPessoaActionListener() {
            @Override
            public void onEditar(Pessoa pessoa) {
                preencherFormularioParaEdicao(pessoa);
            }

            @Override
            public void onExcluir(Pessoa pessoa) {
                confirmarExclusao(pessoa);
            }
        });

        rvCadastros.setLayoutManager(new LinearLayoutManager(this));
        rvCadastros.setAdapter(pessoaAdapter);
    }

    private void configurarAcoes() {
        btnEntrar.setOnClickListener(view -> validarLogin());
        btnCadastrar.setOnClickListener(view -> salvarOuAtualizarCadastro());
        btnLimpar.setOnClickListener(view -> limparFormularioCadastro(true));
        btnSair.setOnClickListener(view -> voltarParaLogin());

        btnBuscarCadastro.setOnClickListener(view -> carregarListaCadastros(texto(etBuscaCadastro)));

        btnLimparBusca.setOnClickListener(view -> {
            etBuscaCadastro.setText("");
            carregarListaCadastros("");
        });

        // Busca dinâmica para melhorar a experiência de uso.
        etBuscaCadastro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não utilizado.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não utilizado.
            }

            @Override
            public void afterTextChanged(Editable s) {
                carregarListaCadastros(s.toString());
            }
        });
    }

    private void configurarMascaras() {
        etCpf.addTextChangedListener(new TextWatcher() {
            private boolean atualizando;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não utilizado.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não utilizado.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (atualizando) {
                    return;
                }

                atualizando = true;
                String digitos = somenteDigitos(s.toString());
                if (digitos.length() > 11) {
                    digitos = digitos.substring(0, 11);
                }
                s.replace(0, s.length(), formatarCpfParcial(digitos));
                atualizando = false;
            }
        });

        etCep.addTextChangedListener(new TextWatcher() {
            private boolean atualizando;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não utilizado.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não utilizado.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (atualizando) {
                    return;
                }

                atualizando = true;
                String digitos = somenteDigitos(s.toString());
                if (digitos.length() > 8) {
                    digitos = digitos.substring(0, 8);
                }
                s.replace(0, s.length(), formatarCepParcial(digitos));
                atualizando = false;
            }
        });
    }

    /**
     * Usuários fixos para autenticação local.
     */
    private void cadastrarUsuariosLogin() {
        listaUsuarios.add(new Usuario("admin", "1234", "Senha numérica simples"));
        listaUsuarios.add(new Usuario("maria", "abc123", "Mistura letras e números"));
        listaUsuarios.add(new Usuario("joao", "senha456", "Começa com a palavra senha"));
    }

    private void validarLogin() {
        String nomeDigitado = texto(etNomeUsuarioLogin);
        String senhaDigitada = texto(etSenhaLogin);

        if (TextUtils.isEmpty(nomeDigitado) || TextUtils.isEmpty(senhaDigitada)) {
            Toast.makeText(this,
                    "Preencha usuário e senha antes de continuar.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuario = buscarUsuarioPorNome(nomeDigitado);
        if (usuario == null) {
            Toast.makeText(this, "Usuário inexistente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!usuario.getSenha().equals(senhaDigitada)) {
            mostrarDialogoDica(usuario);
            return;
        }

        Toast.makeText(this, "Usuário logado!", Toast.LENGTH_SHORT).show();
        atualizarModoCadastro(true, usuario.getNomeUsuario());
    }

    private Usuario buscarUsuarioPorNome(String nomeDigitado) {
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getNomeUsuario().equalsIgnoreCase(nomeDigitado)) {
                return usuario;
            }
        }
        return null;
    }

    private void mostrarDialogoDica(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setMessage("Senha incorreta! Gostaria de ver a dica de senha?")
                .setPositiveButton("Sim", (dialog, which) ->
                        Toast.makeText(this, "Dica: " + usuario.getDicaSenha(), Toast.LENGTH_LONG).show())
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void atualizarModoCadastro(boolean mostrarCadastro, String nomeUsuario) {
        if (mostrarCadastro) {
            cardLogin.setVisibility(View.GONE);
            cardCadastro.setVisibility(View.VISIBLE);
            tvSaudacao.setText("Bem-vindo, " + nomeUsuario + "! Cadastre ou gerencie os registros.");
        } else {
            cardCadastro.setVisibility(View.GONE);
            cardLogin.setVisibility(View.VISIBLE);
        }
    }

    private void voltarParaLogin() {
        atualizarModoCadastro(false, null);
        etSenhaLogin.setText("");
        limparFormularioCadastro(false);
        Toast.makeText(this, "Você voltou para o login.", Toast.LENGTH_SHORT).show();
    }

    private void salvarOuAtualizarCadastro() {
        limparErrosCampos();

        String nome = texto(etNomeCompleto);
        String cpfTexto = texto(etCpf);
        String rua = texto(etRua);
        String numeroRua = texto(etNumeroRua);
        String bairro = texto(etBairro);
        String cepTexto = texto(etCep);

        if (TextUtils.isEmpty(nome)
                || TextUtils.isEmpty(cpfTexto)
                || TextUtils.isEmpty(rua)
                || TextUtils.isEmpty(numeroRua)
                || TextUtils.isEmpty(bairro)
                || TextUtils.isEmpty(cepTexto)) {
            Toast.makeText(this,
                    "Preencha todos os campos do cadastro.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String cpfSomenteDigitos = somenteDigitos(cpfTexto);
        String cepSomenteDigitos = somenteDigitos(cepTexto);

        if (!isCpfValido(cpfSomenteDigitos)) {
            tilCpf.setError("CPF inválido.");
            Toast.makeText(this, "CPF inválido. Verifique os dígitos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.cpfJaExiste(cpfSomenteDigitos, idPessoaEmEdicao)) {
            tilCpf.setError("CPF já cadastrado no sistema.");
            Toast.makeText(this, "Esse CPF já existe no banco local.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cepSomenteDigitos.length() != 8) {
            tilCep.setError("CEP inválido. Deve conter 8 dígitos.");
            Toast.makeText(this,
                    "CEP inválido. Use 8 dígitos (com ou sem hífen).",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int generoSelecionadoId = rgGenero.getCheckedRadioButtonId();
        if (generoSelecionadoId == -1) {
            Toast.makeText(this, "Selecione um gênero.", Toast.LENGTH_SHORT).show();
            return;
        }

        String genero;
        if (generoSelecionadoId == R.id.rbMasculino) {
            genero = "Masculino";
        } else if (generoSelecionadoId == R.id.rbFeminino) {
            genero = "Feminino";
        } else {
            genero = "Outro";
        }

        int dia = dpDataNascimento.getDayOfMonth();
        int mes = dpDataNascimento.getMonth() + 1;
        int ano = dpDataNascimento.getYear();
        String dataNascimento = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes, ano);

        Pessoa pessoa = new Pessoa(
                nome,
                cpfSomenteDigitos,
                rua,
                numeroRua,
                bairro,
                cepSomenteDigitos,
                genero,
                dataNascimento
        );

        boolean sucesso;
        if (idPessoaEmEdicao > 0) {
            pessoa.setId(idPessoaEmEdicao);
            sucesso = databaseHelper.atualizarPessoa(pessoa) > 0;
        } else {
            sucesso = databaseHelper.inserirPessoa(pessoa) > 0;
        }

        if (!sucesso) {
            Toast.makeText(this, "Não foi possível salvar o cadastro.", Toast.LENGTH_SHORT).show();
            return;
        }

        String acao = idPessoaEmEdicao > 0 ? "Cadastro atualizado com sucesso!" : "Cadastro salvo com sucesso!";
        String resumo = acao
                + "\nNome: " + nome
                + "\nCPF: " + formatarCpfCompleto(cpfSomenteDigitos)
                + "\nEndereço: " + rua + ", " + numeroRua + " - " + bairro
                + "\nCEP: " + formatarCepCompleto(cepSomenteDigitos)
                + "\nGênero: " + genero
                + "\nNascimento: " + dataNascimento;

        tvResumoCadastro.setText(resumo);

        String filtroAtual = texto(etBuscaCadastro);
        carregarListaCadastros(filtroAtual);

        limparFormularioCadastro(false);
        Toast.makeText(this, acao, Toast.LENGTH_SHORT).show();
    }

    private void preencherFormularioParaEdicao(Pessoa pessoa) {
        idPessoaEmEdicao = pessoa.getId();

        etNomeCompleto.setText(pessoa.getNome());
        etCpf.setText(formatarCpfCompleto(pessoa.getCpf()));
        etRua.setText(pessoa.getRua());
        etNumeroRua.setText(pessoa.getNumero());
        etBairro.setText(pessoa.getBairro());
        etCep.setText(formatarCepCompleto(pessoa.getCep()));

        if ("Masculino".equalsIgnoreCase(pessoa.getGenero())) {
            rgGenero.check(R.id.rbMasculino);
        } else if ("Feminino".equalsIgnoreCase(pessoa.getGenero())) {
            rgGenero.check(R.id.rbFeminino);
        } else {
            rgGenero.check(R.id.rbOutro);
        }

        // Espera o formato dd/MM/yyyy.
        String[] partesData = pessoa.getDataNascimento().split("/");
        if (partesData.length == 3) {
            try {
                int dia = Integer.parseInt(partesData[0]);
                int mes = Integer.parseInt(partesData[1]) - 1;
                int ano = Integer.parseInt(partesData[2]);
                dpDataNascimento.updateDate(ano, mes, dia);
            } catch (NumberFormatException ignored) {
                // Se vier formato inválido, mantém data atual do picker.
            }
        }

        btnCadastrar.setText("Atualizar cadastro");
        tvResumoCadastro.setText("Modo edição ativo para: " + pessoa.getNome());
        Toast.makeText(this, "Editando cadastro selecionado.", Toast.LENGTH_SHORT).show();
    }

    private void confirmarExclusao(Pessoa pessoa) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir cadastro")
                .setMessage("Deseja realmente excluir o cadastro de " + pessoa.getNome() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    int linhas = databaseHelper.excluirPessoa(pessoa.getId());
                    if (linhas > 0) {
                        if (idPessoaEmEdicao == pessoa.getId()) {
                            limparFormularioCadastro(false);
                        }

                        carregarListaCadastros(texto(etBuscaCadastro));
                        Toast.makeText(this, "Cadastro excluído.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao excluir cadastro.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void carregarListaCadastros(String filtro) {
        String filtroTexto = filtro == null ? "" : filtro.trim();
        String filtroCpfDigitos = somenteDigitos(filtroTexto);

        List<Pessoa> lista = databaseHelper.listarPessoas(filtroTexto, filtroCpfDigitos);
        pessoaAdapter.setPessoas(lista);

        tvSemResultados.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void limparFormularioCadastro(boolean limparResumo) {
        idPessoaEmEdicao = -1;

        etNomeCompleto.setText("");
        etCpf.setText("");
        etRua.setText("");
        etNumeroRua.setText("");
        etBairro.setText("");
        etCep.setText("");
        rgGenero.clearCheck();
        dpDataNascimento.updateDate(2000, 0, 1);

        btnCadastrar.setText("Salvar cadastro");
        limparErrosCampos();

        if (limparResumo) {
            tvResumoCadastro.setText("Nenhum cadastro realizado nesta sessão.");
        }
    }

    private void limparErrosCampos() {
        tilCpf.setError(null);
        tilCep.setError(null);
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // Rejeita CPFs com todos os dígitos iguais.
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int[] numeros = new int[11];
        for (int i = 0; i < 11; i++) {
            numeros[i] = Character.getNumericValue(cpf.charAt(i));
        }

        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += numeros[i] * (10 - i);
        }
        int digito1 = (soma1 * 10) % 11;
        if (digito1 == 10) {
            digito1 = 0;
        }

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += numeros[i] * (11 - i);
        }
        int digito2 = (soma2 * 10) % 11;
        if (digito2 == 10) {
            digito2 = 0;
        }

        return digito1 == numeros[9] && digito2 == numeros[10];
    }

    private String somenteDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    private String formatarCpfParcial(String digitos) {
        if (digitos.length() <= 3) {
            return digitos;
        }
        if (digitos.length() <= 6) {
            return digitos.substring(0, 3) + "." + digitos.substring(3);
        }
        if (digitos.length() <= 9) {
            return digitos.substring(0, 3) + "."
                    + digitos.substring(3, 6) + "."
                    + digitos.substring(6);
        }
        return digitos.substring(0, 3) + "."
                + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-"
                + digitos.substring(9);
    }

    private String formatarCepParcial(String digitos) {
        if (digitos.length() <= 5) {
            return digitos;
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }

    private String formatarCpfCompleto(String cpfSomenteDigitos) {
        if (cpfSomenteDigitos == null || cpfSomenteDigitos.length() != 11) {
            return cpfSomenteDigitos;
        }
        return cpfSomenteDigitos.substring(0, 3) + "."
                + cpfSomenteDigitos.substring(3, 6) + "."
                + cpfSomenteDigitos.substring(6, 9) + "-"
                + cpfSomenteDigitos.substring(9);
    }

    private String formatarCepCompleto(String cepSomenteDigitos) {
        if (cepSomenteDigitos == null || cepSomenteDigitos.length() != 8) {
            return cepSomenteDigitos;
        }
        return cepSomenteDigitos.substring(0, 5) + "-" + cepSomenteDigitos.substring(5);
    }

    private String texto(EditText editText) {
        return editText.getText().toString().trim();
    }
}
