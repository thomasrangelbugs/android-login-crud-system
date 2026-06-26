package com.example.loginsistema;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco local SQLite para armazenar os cadastros de pessoa física.
 */
public class PessoaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NOME = "cadastros_pessoas.db";
    private static final int DB_VERSAO = 2;

    private static final String TABELA_PESSOAS = "pessoas";

    private static final String COL_ID = "id";
    private static final String COL_NOME = "nome";
    private static final String COL_CPF = "cpf";
    private static final String COL_RUA = "rua";
    private static final String COL_NUMERO = "numero";
    private static final String COL_BAIRRO = "bairro";
    private static final String COL_CEP = "cep";
    private static final String COL_GENERO = "genero";
    private static final String COL_DATA_NASC = "data_nasc";

    public PessoaDatabaseHelper(Context context) {
        super(context, DB_NOME, null, DB_VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCriacao = "CREATE TABLE " + TABELA_PESSOAS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOME + " TEXT NOT NULL, "
                + COL_CPF + " TEXT NOT NULL UNIQUE, "
                + COL_RUA + " TEXT NOT NULL, "
                + COL_NUMERO + " TEXT NOT NULL, "
                + COL_BAIRRO + " TEXT NOT NULL, "
                + COL_CEP + " TEXT NOT NULL, "
                + COL_GENERO + " TEXT NOT NULL, "
                + COL_DATA_NASC + " TEXT NOT NULL"
                + ");";

        db.execSQL(sqlCriacao);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_PESSOAS);
        onCreate(db);
    }

    public long inserirPessoa(Pessoa pessoa) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABELA_PESSOAS, null, toContentValues(pessoa));
    }

    public int atualizarPessoa(Pessoa pessoa) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(
                TABELA_PESSOAS,
                toContentValues(pessoa),
                COL_ID + " = ?",
                new String[]{String.valueOf(pessoa.getId())}
        );
    }

    public int excluirPessoa(long idPessoa) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA_PESSOAS, COL_ID + " = ?", new String[]{String.valueOf(idPessoa)});
    }

    /**
     * Verifica se já existe cadastro com o mesmo CPF.
     * Se idIgnorar > 0, ignora esse registro (uso no update).
     */
    public boolean cpfJaExiste(String cpfSomenteDigitos, long idIgnorar) {
        SQLiteDatabase db = getReadableDatabase();

        String selection;
        String[] selectionArgs;

        if (idIgnorar > 0) {
            selection = COL_CPF + " = ? AND " + COL_ID + " != ?";
            selectionArgs = new String[]{cpfSomenteDigitos, String.valueOf(idIgnorar)};
        } else {
            selection = COL_CPF + " = ?";
            selectionArgs = new String[]{cpfSomenteDigitos};
        }

        long count = DatabaseUtils.queryNumEntries(db, TABELA_PESSOAS, selection, selectionArgs);
        return count > 0;
    }

    /**
     * Lê registros filtrando por nome e/ou CPF.
     */
    public List<Pessoa> listarPessoas(String filtroTexto, String filtroCpfSomenteDigitos) {
        List<Pessoa> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;

        boolean temFiltroTexto = filtroTexto != null && !filtroTexto.trim().isEmpty();
        boolean temFiltroCpf = filtroCpfSomenteDigitos != null && !filtroCpfSomenteDigitos.isEmpty();

        if (temFiltroTexto && temFiltroCpf) {
            selection = COL_NOME + " LIKE ? OR " + COL_CPF + " LIKE ?";
            selectionArgs = new String[]{"%" + filtroTexto.trim() + "%", "%" + filtroCpfSomenteDigitos + "%"};
        } else if (temFiltroTexto) {
            selection = COL_NOME + " LIKE ?";
            selectionArgs = new String[]{"%" + filtroTexto.trim() + "%"};
        } else if (temFiltroCpf) {
            selection = COL_CPF + " LIKE ?";
            selectionArgs = new String[]{"%" + filtroCpfSomenteDigitos + "%"};
        }

        Cursor cursor = db.query(
                TABELA_PESSOAS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_ID + " DESC"
        );

        try {
            int idxId = cursor.getColumnIndexOrThrow(COL_ID);
            int idxNome = cursor.getColumnIndexOrThrow(COL_NOME);
            int idxCpf = cursor.getColumnIndexOrThrow(COL_CPF);
            int idxRua = cursor.getColumnIndexOrThrow(COL_RUA);
            int idxNumero = cursor.getColumnIndexOrThrow(COL_NUMERO);
            int idxBairro = cursor.getColumnIndexOrThrow(COL_BAIRRO);
            int idxCep = cursor.getColumnIndexOrThrow(COL_CEP);
            int idxGenero = cursor.getColumnIndexOrThrow(COL_GENERO);
            int idxDataNasc = cursor.getColumnIndexOrThrow(COL_DATA_NASC);

            while (cursor.moveToNext()) {
                Pessoa pessoa = new Pessoa(
                        cursor.getLong(idxId),
                        cursor.getString(idxNome),
                        cursor.getString(idxCpf),
                        cursor.getString(idxRua),
                        cursor.getString(idxNumero),
                        cursor.getString(idxBairro),
                        cursor.getString(idxCep),
                        cursor.getString(idxGenero),
                        cursor.getString(idxDataNasc)
                );

                lista.add(pessoa);
            }
        } finally {
            cursor.close();
        }

        return lista;
    }

    /**
     * Insere alguns cadastros fictícios somente quando a tabela estiver vazia.
     */
    public void inserirDadosFicticiosSeNecessario() {
        SQLiteDatabase db = getWritableDatabase();
        long quantidade = DatabaseUtils.queryNumEntries(db, TABELA_PESSOAS);

        if (quantidade == 0) {
            inserirPessoa(new Pessoa(
                    "Ana Souza",
                    "11144477735",
                    "Rua das Flores",
                    "120",
                    "Centro",
                    "01001000",
                    "Feminino",
                    "15/03/1998"
            ));

            inserirPessoa(new Pessoa(
                    "Carlos Lima",
                    "12345678909",
                    "Avenida Brasil",
                    "450",
                    "Jardins",
                    "04567000",
                    "Masculino",
                    "08/11/1995"
            ));

            inserirPessoa(new Pessoa(
                    "Patricia Gomes",
                    "52998224725",
                    "Rua do Sol",
                    "89",
                    "Nova Esperanca",
                    "13015000",
                    "Outro",
                    "22/07/2001"
            ));
        }
    }

    private ContentValues toContentValues(Pessoa pessoa) {
        ContentValues values = new ContentValues();
        values.put(COL_NOME, pessoa.getNome());
        values.put(COL_CPF, pessoa.getCpf());
        values.put(COL_RUA, pessoa.getRua());
        values.put(COL_NUMERO, pessoa.getNumero());
        values.put(COL_BAIRRO, pessoa.getBairro());
        values.put(COL_CEP, pessoa.getCep());
        values.put(COL_GENERO, pessoa.getGenero());
        values.put(COL_DATA_NASC, pessoa.getDataNascimento());
        return values;
    }
}
