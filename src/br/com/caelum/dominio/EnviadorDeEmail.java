package br.com.caelum.dominio;

import br.com.caelum.leilao.dominio.Leilao;

public interface EnviadorDeEmail {
	void envia(Leilao leilao);

}
