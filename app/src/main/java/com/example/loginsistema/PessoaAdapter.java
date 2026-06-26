package com.example.loginsistema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter da RecyclerView para exibir os cadastros no banco local.
 */
public class PessoaAdapter extends RecyclerView.Adapter<PessoaAdapter.PessoaViewHolder> {

    public interface OnPessoaActionListener {
        void onEditar(Pessoa pessoa);

        void onExcluir(Pessoa pessoa);
    }

    private final List<Pessoa> listaPessoas = new ArrayList<>();
    private final OnPessoaActionListener actionListener;

    public PessoaAdapter(OnPessoaActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setPessoas(List<Pessoa> pessoas) {
        listaPessoas.clear();
        if (pessoas != null) {
            listaPessoas.addAll(pessoas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PessoaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pessoa, parent, false);
        return new PessoaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PessoaViewHolder holder, int position) {
        Pessoa pessoa = listaPessoas.get(position);

        holder.tvNome.setText(pessoa.getNome());

        String linhaCpfGenero = "CPF: " + formatarCpf(pessoa.getCpf()) + "  |  " + pessoa.getGenero();
        holder.tvCpfGenero.setText(linhaCpfGenero);

        holder.tvNascimento.setText("Nascimento: " + pessoa.getDataNascimento());

        String endereco = pessoa.getRua() + ", " + pessoa.getNumero()
                + " - " + pessoa.getBairro()
                + " | CEP: " + formatarCep(pessoa.getCep());
        holder.tvEndereco.setText(endereco);

        holder.btnEditar.setOnClickListener(v -> actionListener.onEditar(pessoa));
        holder.btnExcluir.setOnClickListener(v -> actionListener.onExcluir(pessoa));
    }

    @Override
    public int getItemCount() {
        return listaPessoas.size();
    }

    static class PessoaViewHolder extends RecyclerView.ViewHolder {

        TextView tvNome;
        TextView tvCpfGenero;
        TextView tvNascimento;
        TextView tvEndereco;
        MaterialButton btnEditar;
        MaterialButton btnExcluir;

        PessoaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvItemNome);
            tvCpfGenero = itemView.findViewById(R.id.tvItemCpfGenero);
            tvNascimento = itemView.findViewById(R.id.tvItemNascimento);
            tvEndereco = itemView.findViewById(R.id.tvItemEndereco);
            btnEditar = itemView.findViewById(R.id.btnEditarPessoa);
            btnExcluir = itemView.findViewById(R.id.btnExcluirPessoa);
        }
    }

    private String formatarCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "."
                + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-"
                + digitos.substring(9);
    }

    private String formatarCep(String cep) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            return cep;
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }
}
