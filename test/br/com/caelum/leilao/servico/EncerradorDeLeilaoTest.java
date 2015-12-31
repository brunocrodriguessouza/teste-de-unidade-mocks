package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.inOrder;

//import static org.mockito.Mockito.*;
//Esse import ja e o suficiente para evitar esse numero de import


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.dominio.EnviadorDeEmail;
import br.com.caelum.dominio.RepositorioDeLeiloes;
import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;

public class EncerradorDeLeilaoTest {
	
	private EnviadorDeEmail carteiroFalso;
	private RepositorioDeLeiloes daoFalso;

	@Before
	public void setUp(){
		this.carteiroFalso = mock(EnviadorDeEmail.class);
		this.daoFalso = mock(LeilaoDao.class);
	}

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();

		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("Smartv LCD full HD")
				.naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga)
				.constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());

	}
	
    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
            .naData(ontem).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
        
        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
    }
    
    @Test
    public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {

        when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
    }
    
    @Test
    public void deveAtualizarLeiloesEncerrados(){
    	Calendar antiga = Calendar.getInstance();
    	antiga.set(1999, 1, 20);
    	
    	Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
    	
    	when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
    	
    	EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
    	encerrador.encerra();
    	
    	verify(daoFalso, times(1)).atualiza(leilao1);
    	
    	//Apenas para treino
    	verify(daoFalso, atLeast(1)).atualiza(leilao1);
    	verify(daoFalso, atLeastOnce()).atualiza(leilao1);
    	verify(daoFalso, atMost(1)).atualiza(leilao1);
    }
    
    @Test
    public void deveEnviarEmailAposPersistirLeilaoEncerrado(){
    	Calendar antiga = Calendar.getInstance();
    	antiga.set(1999, 1, 20);
    	
    	Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();
    	
    	when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
    	EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
    	
    	encerrador.encerra();
    	carteiroFalso.envia(leilao1);
    	
    	InOrder inOrder = inOrder(daoFalso, carteiroFalso);
    	inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
    	inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
    }

}
