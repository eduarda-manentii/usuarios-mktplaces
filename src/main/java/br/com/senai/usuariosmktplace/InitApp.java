package br.com.senai.usuariosmktplace;

import br.com.senai.usuariosmktplace.core.service.UsuarioService;

public class InitApp {

	public static void main(String[] args) {
		System.out.println(new UsuarioService().resetarSenha("eduarda.manenti"));
	}

}
