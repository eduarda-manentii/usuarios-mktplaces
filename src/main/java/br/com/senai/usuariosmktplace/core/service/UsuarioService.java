package br.com.senai.usuariosmktplace.core.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import br.com.senai.usuariosmktplace.core.dao.DaoUsuario;
import br.com.senai.usuariosmktplace.core.dao.FactoryDao;
import br.com.senai.usuariosmktplace.core.domain.Usuario;

public class UsuarioService {

	private DaoUsuario daoUsuario;

	public UsuarioService() {
		this.daoUsuario = FactoryDao.getInstance().getDaoUsuario();
	}

	public Usuario criarUsuario(String nomeCompleto, String senha) {
		this.valida(nomeCompleto, senha);
		String nomeFormatado = formatarNome(nomeCompleto);
		String login = gerarLoginPor(nomeFormatado);
		String senhaHash = gerarHashDa(senha);
		Usuario novoUsuario = new Usuario(login, senhaHash, nomeFormatado);
		this.daoUsuario.inserir(novoUsuario);
		Usuario usuarioSalvo = this.daoUsuario.buscarPor(login);
		return usuarioSalvo;
	}

	private String removerAcentoDo(String nomeCompleto) {
		return Normalizer.normalize(nomeCompleto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	private List<String> fracionar(String nomeCompleto) {
		List<String> nomeFracionado = new ArrayList<String>();
		if (Strings.isNullOrEmpty(nomeCompleto)) {
			nomeCompleto = nomeCompleto.trim();
			String[] partesDoNome = nomeCompleto.split(" ");
			for (String parte : partesDoNome) {
				boolean isNaoContemArtigo = !(parte.equalsIgnoreCase("de") || parte.equalsIgnoreCase("e")
						|| parte.equalsIgnoreCase("do") || parte.equalsIgnoreCase("dos") || parte.equalsIgnoreCase("da")
						|| parte.equalsIgnoreCase("das"));
				if (isNaoContemArtigo) {
					nomeFracionado.add(parte.toLowerCase());
				}
			}
		}
		return nomeFracionado;
	}

	private String gerarLoginPor(String nomeCompleto) {
		nomeCompleto = removerAcentoDo(nomeCompleto);
		List<String> partesDoNome = fracionar(nomeCompleto);
		String loginGerado = null;
		Usuario usuarioEncontrado = null;
		if (!partesDoNome.isEmpty()) {
			for (int i = 1; i < partesDoNome.size(); i++) {
				loginGerado = partesDoNome.get(0) + "." + partesDoNome.get(i);
				usuarioEncontrado = buscarPor(loginGerado);
				if (usuarioEncontrado == null) {
					return loginGerado;
				}
			}
			int proximoSequencial = 0;
			String loginDisponivel = null;
			while (usuarioEncontrado != null) {
				loginDisponivel = loginGerado + ++proximoSequencial;
				usuarioEncontrado = buscarPor(loginDisponivel);
			}
			loginGerado = loginDisponivel;
		}
		return loginGerado;
	}

	private String gerarHashDa(String senha) {
		return new DigestUtils(MessageDigestAlgorithms.SHA3_256).digestAsHex(senha);
	}

	private String formatarNome(String nomeCompleto) {
		String[] partesDoNome = nomeCompleto.trim().split(" ");
		StringBuilder nomeFormatado = new StringBuilder();

		for (String parte : partesDoNome) {
			if (!parte.isEmpty()) {
				nomeFormatado.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1).toLowerCase())
						.append(" ");
			}
		}

		return nomeFormatado.toString().trim();
	}

	@SuppressWarnings("deprecation")
	private void valida(String senha) {
		boolean isSenhaValida = !Strings.isNullOrEmpty(senha) && senha.length() > 5 && senha.length() < 16;
		Preconditions.checkArgument(isSenhaValida, "A senha é obrigatória e deve ter entre 6 e 15 caracteres.");

		boolean isContemLetra = CharMatcher.inRange('a', 'z').countIn(senha.toLowerCase()) > 0;
		boolean isContemNumero = CharMatcher.inRange('0', '9').countIn(senha) > 0;
		boolean isCaracterInvalido = !CharMatcher.javaLetterOrDigit().matchesAllOf(senha);

		Preconditions.checkArgument(isContemLetra && isContemNumero && !isCaracterInvalido,
				"A senha deve conter letras e numeros.");
	}

	private void valida(String nomeCompleto, String senha) {
		List<String> partesDoNome = fracionar(nomeCompleto);
		boolean isNomeCompleto = partesDoNome.size() > 1;
		boolean isNomeValido = !Strings.isNullOrEmpty(nomeCompleto) && isNomeCompleto && nomeCompleto.length() <= 120
				&& nomeCompleto.length() >= 5;
		Preconditions.checkArgument(isNomeValido,
				"O nome é obrigatório nome deve conter entre" + " 5 a 50 caracteres e conter sobrenome também.");
		nomeCompleto = nomeCompleto.replaceAll("\\s+", "");
		this.valida(senha);
	}

	private Usuario buscarPor(String loginGerado) {
		Usuario encontrado = daoUsuario.buscarPor(loginGerado);
		return encontrado;
	}

}
