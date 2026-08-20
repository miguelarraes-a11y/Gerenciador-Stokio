package com.senai.pi.stokio.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.Agendamento;
import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.repositories.AgendamentoRepository;
import com.senai.pi.stokio.repositories.MateriaisRepository;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private MateriaisRepository materiaisRepository;

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public List<Agendamento> listarPorStatus(String status) {
        return agendamentoRepository.findByStatus(status);
    }

    public Optional<Agendamento> buscarPorId(Integer id) {
        return agendamentoRepository.findById(id);
    }

    public Agendamento criar(Agendamento agendamento) {
        validarDatas(agendamento.getDataInicio(), agendamento.getDataFim());
        
        if (agendamento.getMaterial() != null && agendamento.getMaterial().getId() != null) {
            validarDisponibilidadeHorario(
                agendamento.getMaterial().getId(),
                agendamento.getDataInicio(),
                agendamento.getDataFim(),
                null
            );
        }

        if (agendamento.getStatus() == null || agendamento.getStatus().isEmpty()) {
            agendamento.setStatus("PENDENTE");
        }

        return agendamentoRepository.save(agendamento);
    }

    public Agendamento atualizar(Integer id, Agendamento dadosAtualizados) {
        Agendamento existente = agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + id));

        validarDatas(dadosAtualizados.getDataInicio(), dadosAtualizados.getDataFim());

        Integer materialId = dadosAtualizados.getMaterial() != null ? dadosAtualizados.getMaterial().getId() : null;
        if (materialId != null) {
            validarDisponibilidadeHorario(
                materialId,
                dadosAtualizados.getDataInicio(),
                dadosAtualizados.getDataFim(),
                id
            );
        }

        existente.setDescricao(dadosAtualizados.getDescricao());
        existente.setDataInicio(dadosAtualizados.getDataInicio());
        existente.setDataFim(dadosAtualizados.getDataFim());
        existente.setQuantidade(dadosAtualizados.getQuantidade());
        existente.setObservacoes(dadosAtualizados.getObservacoes());
        if (dadosAtualizados.getStatus() != null) {
            existente.setStatus(dadosAtualizados.getStatus());
        }
        if (dadosAtualizados.getMaterial() != null) {
            existente.setMaterial(dadosAtualizados.getMaterial());
        }
        if (dadosAtualizados.getUsuario() != null) {
            existente.setUsuario(dadosAtualizados.getUsuario());
        }

        return agendamentoRepository.save(existente);
    }

    public Agendamento atualizarStatus(Integer id, String novoStatus) {
        Agendamento existente = agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + id));
        existente.setStatus(novoStatus.toUpperCase());
        return agendamentoRepository.save(existente);
    }

    public void deletar(Integer id) {
        agendamentoRepository.deleteById(id);
    }

    private void validarDatas(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }
        if (fim.isBefore(inicio) || fim.isEqual(inicio)) {
            throw new IllegalArgumentException("A data/hora de fim deve ser posterior à data/hora de início.");
        }
    }

    public void validarDisponibilidadeHorario(Integer materialId, LocalDateTime inicio, LocalDateTime fim, Integer agendamentoIdIgnorado) {
        boolean temIdIgnorado = agendamentoIdIgnorado != null;
        List<Agendamento> conflitos = agendamentoRepository.findConflitos(
                materialId, inicio, fim, agendamentoIdIgnorado, temIdIgnorado
        );
        if (!conflitos.isEmpty()) {
            throw new IllegalStateException("Conflito de horário! O material selecionado já possui agendamento confirmado ou pendente neste período.");
        }
    }
}
