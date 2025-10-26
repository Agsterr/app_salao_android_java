package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.os.Environment;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class ProdutoActivity extends AppCompatActivity {

    private Button buttonNovoProduto;
    private Button buttonVerRecebimentos;
    private Button buttonVerVendas;
    private Button buttonVoltarProdutos;
    private EditText editTextBuscarProdutos;
    private ListView listViewProdutos;

    private ProdutoDAO produtoDAO;
    private ArrayAdapter<Produto> produtosAdapter;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri fotoUriTemp;
    private Uri fotoUriSelecionada;
    private ImageView dialogImageViewFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Produtos");
        }

        produtoDAO = new ProdutoDAO(this);
        produtoDAO.open();

        buttonNovoProduto = findViewById(R.id.buttonNovoProduto);
        buttonVerRecebimentos = findViewById(R.id.buttonVerRecebimentos);
        buttonVerVendas = findViewById(R.id.buttonVerVendas);
        buttonVoltarProdutos = findViewById(R.id.buttonVoltarProdutos);
        editTextBuscarProdutos = findViewById(R.id.editTextBuscarProdutos);
        listViewProdutos = findViewById(R.id.listViewProdutos);

        buttonVoltarProdutos.setOnClickListener(v -> finish());
        buttonNovoProduto.setOnClickListener(v -> mostrarDialogProduto(null));
        buttonVerRecebimentos.setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoActivity.this, RecebimentosActivity.class);
            startActivity(intent);
        });
        buttonVerVendas.setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoActivity.this, VendasActivity.class);
            startActivity(intent);
        });

        editTextBuscarProdutos.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (produtosAdapter != null) produtosAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        registerForContextMenu(listViewProdutos);
        atualizarListaProdutos();

        // Inicializa launchers de câmera e galeria
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && dialogImageViewFoto != null && fotoUriTemp != null) {
                dialogImageViewFoto.setImageURI(fotoUriTemp);
                fotoUriSelecionada = fotoUriTemp;
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                fotoUriSelecionada = uri;
                if (dialogImageViewFoto != null) {
                    dialogImageViewFoto.setImageURI(uri);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        produtoDAO.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void atualizarListaProdutos() {
        List<Produto> produtos = produtoDAO.getAllProdutos();
        if (produtosAdapter == null) {
            produtosAdapter = new ProdutoListAdapter(this);
            listViewProdutos.setAdapter(produtosAdapter);
        }
        produtosAdapter.clear();
        for (Produto p : produtos) {
            produtosAdapter.add(p);
        }
        produtosAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listViewProdutos) {
            getMenuInflater().inflate(R.menu.menu_contexto_produto, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Produto produtoSelecionado = produtosAdapter.getItem(position);
        if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogProduto(produtoSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            confirmarApagarProduto(produtoSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_vender) {
            Intent intent = new Intent(this, RegistrarVendaActivity.class);
            intent.putExtra("produto_id", produtoSelecionado.getId());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_compartilhar) {
            StringBuilder sb = new StringBuilder();
            sb.append("Produto: ").append(produtoSelecionado.getNome());
            sb.append("\nValor padrão: ").append(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(produtoSelecionado.getValorPadrao()));
            if (produtoSelecionado.getDescricao() != null && !produtoSelecionado.getDescricao().trim().isEmpty()) {
                sb.append("\nDescrição: ").append(produtoSelecionado.getDescricao());
            }
            String texto = sb.toString();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (produtoSelecionado.getImagemUri() != null && !produtoSelecionado.getImagemUri().trim().isEmpty()) {
                Uri imageUri = Uri.parse(produtoSelecionado.getImagemUri());
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, texto);
            } else {
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, texto);
            }
            startActivity(Intent.createChooser(shareIntent, "Compartilhar produto"));
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private Uri createImageUri() {
        try {
            File imagesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "produtos");
            if (!imagesDir.exists()) imagesDir.mkdirs();
            File imageFile = File.createTempFile("produto_", ".jpg", imagesDir);
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void mostrarDialogProduto(final Produto produtoExistente) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(produtoExistente == null ? "Novo Produto" : "Editar Produto");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_produto, null);
        EditText editTextNome = dialogView.findViewById(R.id.editTextNomeProduto);
        EditText editTextValor = dialogView.findViewById(R.id.editTextValorProduto);
        EditText editTextDescricao = dialogView.findViewById(R.id.editTextDescricaoProduto);
        ImageView imageViewFoto = dialogView.findViewById(R.id.imageViewProdutoFoto);
        Button buttonSelecionarGaleria = dialogView.findViewById(R.id.buttonSelecionarGaleria);
        Button buttonTirarFoto = dialogView.findViewById(R.id.buttonTirarFoto);
        Button buttonRemoverFoto = dialogView.findViewById(R.id.buttonRemoverFoto);
        dialogImageViewFoto = imageViewFoto;

        if (produtoExistente != null) {
            editTextNome.setText(produtoExistente.getNome());
            editTextValor.setText(String.valueOf(produtoExistente.getValorPadrao()));
            editTextDescricao.setText(produtoExistente.getDescricao());
            if (produtoExistente.getImagemUri() != null) {
                try {
                    fotoUriSelecionada = Uri.parse(produtoExistente.getImagemUri());
                    imageViewFoto.setImageURI(fotoUriSelecionada);
                } catch (Exception ignored) {}
            }
        } else {
            fotoUriSelecionada = null;
        }

        buttonTirarFoto.setOnClickListener(v -> {
            Uri uri = createImageUri();
            if (uri != null) {
                fotoUriTemp = uri;
                cameraLauncher.launch(uri);
            }
        });
        buttonSelecionarGaleria.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });
        buttonRemoverFoto.setOnClickListener(v -> {
            fotoUriSelecionada = null;
            imageViewFoto.setImageDrawable(null);
            imageViewFoto.setImageResource(0);
        });

        builder.setView(dialogView);
        // Evitar fechar automaticamente; validar antes de salvar
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dlg -> {
            android.widget.Button positiveButton = alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String nome = editTextNome.getText().toString().trim();
                String valorStr = editTextValor.getText().toString().trim();
                String descricao = editTextDescricao.getText().toString().trim();
                if (nome.isEmpty()) {
                    Toast.makeText(this, "Informe o nome do produto", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (valorStr.isEmpty()) {
                    Toast.makeText(this, "Informe o valor", Toast.LENGTH_SHORT).show();
                    return;
                }
                double valor;
                try {
                    valor = Double.parseDouble(valorStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (produtoExistente == null) {
                    Produto novo = new Produto();
                    novo.setNome(nome);
                    novo.setValorPadrao(valor);
                    novo.setDescricao(descricao);
                    novo.setImagemUri(fotoUriSelecionada != null ? fotoUriSelecionada.toString() : null);
                    long id = produtoDAO.inserirProduto(novo);
                    Toast.makeText(this, "Produto salvo (ID: " + id + ")", Toast.LENGTH_SHORT).show();
                } else {
                    produtoExistente.setNome(nome);
                    produtoExistente.setValorPadrao(valor);
                    produtoExistente.setDescricao(descricao);
                    produtoExistente.setImagemUri(fotoUriSelecionada != null ? fotoUriSelecionada.toString() : null);
                    int rows = produtoDAO.atualizarProduto(produtoExistente);
                    if (rows > 0) {
                        Toast.makeText(this, "Produto atualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Falha ao atualizar", Toast.LENGTH_SHORT).show();
                    }
                }
                atualizarListaProdutos();
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    private void confirmarApagarProduto(Produto produto) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Apagar Produto");
        builder.setMessage("Deseja apagar o produto '" + produto.getNome() + "'?");
        builder.setPositiveButton("Apagar", (dialog, which) -> {
            int rows = produtoDAO.apagarProduto(produto.getId());
            if (rows > 0) {
                Toast.makeText(this, "Produto apagado", Toast.LENGTH_SHORT).show();
                atualizarListaProdutos();
            } else {
                Toast.makeText(this, "Falha ao apagar", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Adapter customizado para mostrar imagem ao lado do nome
    private static class ProdutoListAdapter extends ArrayAdapter<Produto> {
        private final LayoutInflater inflater;

        ProdutoListAdapter(Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_produto, parent, false);
            }
            Produto produto = getItem(position);
            ImageView imageView = view.findViewById(R.id.imageThumb);
            TextView textNome = view.findViewById(R.id.textNome);
            TextView textPreco = view.findViewById(R.id.textPreco);

            if (produto != null) {
                textNome.setText(produto.getNome());
                String precoFormatado = NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(produto.getValorPadrao());
                textPreco.setText(precoFormatado);
                String uriStr = produto.getImagemUri();
                if (uriStr != null && !uriStr.isEmpty()) {
                    try {
                        imageView.setImageURI(Uri.parse(uriStr));
                    } catch (Exception e) {
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    }
                } else {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }
            }
            return view;
        }
    }
}