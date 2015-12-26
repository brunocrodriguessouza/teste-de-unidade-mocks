package br.com.caelum.dominio;

import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public interface RepositorioDeLeiloes {
	void salva(Leilao leilao);

	List<Leilao> encerrados();

	List<Leilao> correntes();

	void atualiza(Leilao leilao);
}
