package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;
import static pt.iflow.blocks.P17040.utils.FileValidationUtils.isValidDomainValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ValidateCCIN extends BlockP17040Validate {

	public BlockP17040ValidateCCIN(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {

		ArrayList<ValidationError> result = new ArrayList<>();

		// infInstList
		List<Integer> infInstIdList = retrieveSimpleField(connection, userInfo,
				"select infInst.id from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infInstId : infInstIdList) {
			// lstCaracEsp
			ArrayList<String> tpCaractEspAux = validatelstCaracEsp( connection,  userInfo, result, infInstId);
			
			HashMap<String, Object> infInstValues = fillAtributtes(null, connection, userInfo,
					"select * from infInst where id = {0} ", new Object[] { infInstId });

			// dtRefInst
			Date dtRefInst = (Date) infInstValues.get("dtRefInst");
			if (dtRefInst == null)
				result.add(new ValidationError("CI001", "infInst", "dtRefInst", infInstId));
			if (dtRefInst != null && dtRefInst.after(new Date()))
				result.add(new ValidationError("CI002", "infInst", "dtRefInst", infInstId, dtRefInst));

			// idCont
			String idCont = (String) infInstValues.get("idCont");

			// idInst
			String idInst = (String) infInstValues.get("idInst");
			if (retrieveSimpleField(connection, userInfo,
					"select infInst.idInst from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} and infInst.idInst = '{1}' ",
					new Object[] { crcId, idInst }).size() > 1)
				result.add(new ValidationError("EF012", "infInst", "idInst", infInstId));

			// balcao
			String balcao = (String) infInstValues.get("balcao");

			// projFinan
			String projFinan = (String) infInstValues.get("projFinan");
			if (!isValidDomainValue(userInfo, connection, "T_EFP", projFinan))
				result.add(new ValidationError("CI007", "infInst", "projFinan", infInstId, projFinan));

			// idContSind
			String idContSind = (String) infInstValues.get("idContSind");

			// litigJud
			String litigJud = (String) infInstValues.get("litigJud");
			if (StringUtils.isBlank(litigJud))
				result.add(new ValidationError("CI010", "infInst", "litigJud", infInstId, litigJud));
			if (!isValidDomainValue(userInfo, connection, "T_LJU", litigJud))
				result.add(new ValidationError("CI011", "infInst", "litigJud", infInstId, litigJud));

			// IEB
			String IEB = (String) infInstValues.get("IEB");
			if (retrieveSimpleField(connection, userInfo,
					"select id from infInst where IEB = ''{1}'' and !(idCont= ''{1}'' and idInst = ''{2}'')",
					new Object[] { IEB, idCont, idInst }).size() > 0)
				result.add(new ValidationError("CI058", "infInst", "IEB", infInstId, IEB));
			;

			// paisLegis
			String paisLegis = (String) infInstValues.get("paisLegis");
			if (!isValidDomainValue(userInfo, connection, "T_TER", paisLegis))
				result.add(new ValidationError("CI013", "infInst", "paisLegis", infInstId, paisLegis));

			// canalComer
			String canalComer = (String) infInstValues.get("canalComer");
			Date dtIniInst = (Date) infInstValues.get("dtIniInst");
			Calendar fimDe2018 = Calendar.getInstance();
			fimDe2018.set(2018, 12, 31, 24, 0);
			if (!isValidDomainValue(userInfo, connection, "T_CCO", canalComer))
				result.add(new ValidationError("CI104", "infInst", "canalComer", infInstId, canalComer));
			if (StringUtils.isBlank(canalComer) && fimDe2018.getTime().before(dtIniInst)
					&& (tpCaractEspAux.contains("005") || tpCaractEspAux.contains("006")))
				result.add(new ValidationError("CI014", "infInst", "canalComer", infInstId));

			// clausRenun
			String clausRenun = (String) infInstValues.get("clausRenun");
			if (!isValidDomainValue(userInfo, connection, "T_REN", clausRenun))
				result.add(new ValidationError("CI015", "infInst", "clausRenun", infInstId, clausRenun));

			// subvProtocolo
			String subvProtocolo = (String) infInstValues.get("subvProtocolo");
			if (!isValidDomainValue(userInfo, connection, "T_SPR", subvProtocolo))
				result.add(new ValidationError("CI027", "infInst", "subvProtocolo", infInstId, subvProtocolo));
			if (StringUtils.isBlank(subvProtocolo)
					&& (tpCaractEspAux.contains("005") ||tpCaractEspAux.contains("006")))
				result.add(new ValidationError("CI028", "infInst", "subvProtocolo", infInstId));

			// refExtInst
			String refExtInst = (String) infInstValues.get("refExtInst");
			if (StringUtils.isBlank(refExtInst))
				result.add(new ValidationError("CI099", "infInst", "refExtInst", infInstId));
			if (retrieveSimpleField(connection, userInfo,
					"select id from infInst where refExtInst = ''{1}'' and idCont!= ''{1}'' )",
					new Object[] { refExtInst, idCont }).size() > 0)
				result.add(new ValidationError("CI112", "infInst", "refExtInst", infInstId, refExtInst));

			// tpInst
			String tpInst = (String) infInstValues.get("tpInst");
			if (StringUtils.isBlank(tpInst))
				result.add(new ValidationError("CI029", "infInst", "tpInst", infInstId));
			if (!isValidDomainValue(userInfo, connection, "T_TIN", (String) infInstValues.get("tpInst")))
				result.add(new ValidationError("CI031", "infInst", "tpInst", infInstId));
			if (tpCaractEspAux.contains("005")
					&& !(StringUtils.equals("0031", tpInst) || StringUtils.equals("0032", tpInst)
							|| StringUtils.equals("0033", tpInst) || StringUtils.equals("0034", tpInst)
							|| StringUtils.equals("0041", tpInst) || StringUtils.equals("0042", tpInst)
							|| StringUtils.equals("0043", tpInst) || StringUtils.equals("0051", tpInst)
							|| StringUtils.equals("0052", tpInst) || StringUtils.equals("0092", tpInst)
							|| StringUtils.equals("0130", tpInst) || StringUtils.equals("0140", tpInst)))
				result.add(new ValidationError("CI030", "infInst", "tpInst", infInstId));

			// moeda
			String moeda = (String) infInstValues.get("moeda");
			if (StringUtils.isBlank(moeda))
				result.add(new ValidationError("CI032", "infInst", "moeda", infInstId));
			if (!isValidDomainValue(userInfo, connection, "T_DIV", moeda))
				result.add(new ValidationError("CI033", "infInst", "moeda", infInstId, moeda));

			// dtUtilFund
			Date dtUtilFund = (Date) infInstValues.get("dtUtilFund");
			Date dtMat = (Date) infInstValues.get("dtMat");
			if (dtUtilFund != null && dtMat != null && dtUtilFund.after(dtMat))
				result.add(new ValidationError("INC015", "infInst", "dtUtilFund", infInstId, dtUtilFund));
			if (dtUtilFund != null && dtIniInst != null && dtIniInst.after(dtUtilFund))
				result.add(new ValidationError("INC021", "infInst", "dtUtilFund", infInstId, dtUtilFund));

			// dtIniInst
			Date dtIniCarJur = (Date) infInstValues.get("dtIniCarJur");
			Date dtFimCarJur = (Date) infInstValues.get("dtFimCarJur");
			Date dtIniCarCap = (Date) infInstValues.get("dtIniCarCap");
			Date dtFimCarCap = (Date) infInstValues.get("dtFimCarCap");
			Date dtReneg = (Date) infInstValues.get("dtReneg");
			if (dtIniInst == null)
				result.add(new ValidationError("CI034", "infInst", "dtIniInst", infInstId));
			if (dtIniInst != null && dtIniCarJur != null && dtIniInst.after(dtIniCarJur))
				result.add(new ValidationError("INC022", "infInst", "dtIniInst", infInstId, dtIniInst));
			if (dtIniInst != null && dtFimCarJur != null && dtIniInst.after(dtFimCarJur))
				result.add(new ValidationError("INC023", "infInst", "dtIniInst", infInstId, dtIniInst));
			if (dtIniInst != null && dtIniCarCap != null && dtIniInst.after(dtIniCarCap))
				result.add(new ValidationError("INC024", "infInst", "dtIniInst", infInstId, dtIniInst));
			if (dtIniInst != null && dtFimCarCap != null && dtIniInst.after(dtFimCarCap))
				result.add(new ValidationError("INC025", "infInst", "dtIniInst", infInstId, dtIniInst));
			if (dtIniInst != null && dtReneg != null && dtIniInst.after(dtReneg))
				result.add(new ValidationError("INC026", "infInst", "dtIniInst", infInstId, dtIniInst));
			if (dtIniInst != null && dtIniInst.after(dtRefInst))
				result.add(new ValidationError("INC031", "infInst", "dtIniInst", infInstId, dtIniInst));

			// dtMat
			Date dtOriMat = (Date) infInstValues.get("dtOriMat");
			if (dtMat == null)
				result.add(new ValidationError("CI036", "infInst", "dtMat", infInstId));
			if (dtMat != null && dtOriMat != null && !dtMat.equals(dtOriMat))
				result.add(new ValidationError("CI037", "infInst", "dtMat", infInstId));
			if (dtIniInst != null && dtMat != null && dtIniInst.after(dtMat))
				result.add(new ValidationError("CI094", "infInst", "dtMat", infInstId));

			// dtIniCarJur
			if (dtIniCarJur == null)
				result.add(new ValidationError("CI038", "infInst", "dtIniCarJur", infInstId));
			if (dtIniCarJur != null && dtIniInst != null && dtIniCarJur.before(dtIniInst))
				result.add(new ValidationError("CI090", "infInst", "dtIniCarJur", infInstId));

			// dtFimCarJur
			if (dtFimCarJur == null)
				result.add(new ValidationError("CI039", "infInst", "dtFimCarJur", infInstId));
			if (dtFimCarJur != null && dtIniCarJur != null && dtFimCarJur.before(dtIniCarJur))
				result.add(new ValidationError("CI040", "infInst", "dtFimCarJur", infInstId));

			// dtIniCarCap
			if (dtIniCarCap == null)
				result.add(new ValidationError("CI041", "infInst", "dtIniCarCap", infInstId));
			if (dtIniCarCap != null && dtIniInst != null && dtIniCarCap.before(dtIniInst))
				result.add(new ValidationError("CI090", "infInst", "dtIniCarCap", infInstId, dtIniCarCap));

			// dtFimCarCap
			if (dtFimCarCap == null)
				result.add(new ValidationError("CI042", "infInst", "dtFimCarCap", infInstId));
			if (dtFimCarCap != null && dtIniInst != null && dtFimCarCap.before(dtIniInst))
				result.add(new ValidationError("CI043", "infInst", "dtFimCarCap", infInstId, dtFimCarCap));

			// dirReembIme
			if (!isValidDomainValue(userInfo, connection, "T_REB", (String) infInstValues.get("dirReembIme")))
				result.add(new ValidationError("CI044", "infInst", "dirReembIme", infInstId,
						(String) infInstValues.get("dirReembIme")));

			// recurso
			String recurso = (String) infInstValues.get("recurso");
			if (StringUtils.isBlank(recurso) && StringUtils.equals(tpInst, "0081"))
				result.add(new ValidationError("CI045", "infInst", "recurso", infInstId));
			if (!isValidDomainValue(userInfo, connection, "T_RCU", recurso))
				result.add(new ValidationError("CI046", "infInst", "recurso", infInstId, recurso));

			// tpTxJuro
			String tpTxJuro = (String) infInstValues.get("tpTxJuro");
			if (!isValidDomainValue(userInfo, connection, "T_TTJ", (String) infInstValues.get("tpTxJuro")))
				result.add(new ValidationError("CI047", "infInst", "tpTxJuro", infInstId));
			if (StringUtils.isBlank(tpTxJuro) && tpCaractEspAux.contains("005"))
				result.add(new ValidationError("CI048", "infInst", "tpTxJuro", infInstId));
			if (StringUtils.isBlank(tpTxJuro) && tpCaractEspAux.contains("002"))
				result.add(new ValidationError("CI049", "infInst", "tpTxJuro", infInstId));

			// dtOriMat
			if (dtOriMat == null)
				result.add(new ValidationError("CI035", "infInst", "dtOriMat", infInstId));
			if (dtIniInst != null && dtOriMat != null && dtIniInst.after(dtOriMat))
				result.add(new ValidationError("CI094", "infInst", "dtOriMat", infInstId));

			// freqAtualizTx
			String freqAtualizTx = (String) infInstValues.get("freqAtualizTx");
			if (!isValidDomainValue(userInfo, connection, "T_FTJ", freqAtualizTx))
				result.add(new ValidationError("CI050", "infInst", "freqAtualizTx", infInstId, freqAtualizTx));

			// txRef
			String txRef = (String) infInstValues.get("txRef");
			if (!isValidDomainValue(userInfo, connection, "T_TXR", txRef))
				result.add(new ValidationError("CI051", "infInst", "txRef", infInstId, txRef));
			if ((StringUtils.equals(tpTxJuro, "002") && !StringUtils.equals(txRef, "0000"))
					|| (!StringUtils.equals(tpTxJuro, "002") && StringUtils.equals(txRef, "0000")))
				result.add(new ValidationError("CI095", "infInst", "txRef", infInstId, txRef));
			if (StringUtils.isBlank(txRef) && tpCaractEspAux.contains( "002"))
				result.add(new ValidationError("CI108", "infInst", "txRef", infInstId, txRef));

			// TAEG
			BigDecimal TAEG = (BigDecimal) infInstValues.get("TAEG");
			if (TAEG == null && tpCaractEspAux.contains( "005"))
				result.add(new ValidationError("CI098", "infInst", "TAEG", infInstId, TAEG));

			// TAE
			BigDecimal TAE = (BigDecimal) infInstValues.get("TAE");
			Calendar inicioDe2018 = Calendar.getInstance();
			inicioDe2018.set(2018, 1, 1, 0, 0);
			if (TAE == null && tpCaractEspAux.contains("006") && dtIniInst.before(inicioDe2018.getTime()))
				result.add(new ValidationError("CI097", "infInst", "TAE", infInstId, TAE));

			// spread
			BigDecimal spread = (BigDecimal) infInstValues.get("spread");
			if (spread == null && tpCaractEspAux.contains( "006"))
				result.add(new ValidationError("CI052", "infInst", "spread", infInstId));
			if (spread != null && StringUtils.equals(tpTxJuro, "002"))
				result.add(new ValidationError("CI053", "infInst", "spread", infInstId, spread));

			// txMax
			BigDecimal txMax = (BigDecimal) infInstValues.get("txMax");
			if (txMax != null && StringUtils.equals(tpTxJuro, "002"))
				result.add(new ValidationError("CI054", "infInst", "txMax", infInstId, txMax));

			// txMin
			BigDecimal txMin = (BigDecimal) infInstValues.get("txMin");
			if (txMin != null && StringUtils.equals(tpTxJuro, "002"))
				result.add(new ValidationError("CI055", "infInst", "txMin", infInstId, txMin));
			if (txMin != null && txMax != null && txMin.compareTo(txMax) == 1)
				result.add(new ValidationError("CI089", "infInst", "txMin", infInstId, txMin));

			// perFixTx
			Integer perFixTx = (Integer) infInstValues.get("perFixTx");
			if (perFixTx != null && perFixTx < 0)
				result.add(new ValidationError("CI057", "infInst", "perFixTx", infInstId));

			// durPlanoFin
			Integer durPlanoFin = (Integer) infInstValues.get("durPlanoFin");
			if (durPlanoFin != null && durPlanoFin < 0)
				result.add(new ValidationError("CI105", "infInst", "durPlanoFin", infInstId, durPlanoFin));
			if (durPlanoFin == null)
				result.add(new ValidationError("CI100", "infInst", "durPlanoFin", infInstId, durPlanoFin));

			// finalidade
			String finalidade = (String) infInstValues.get("finalidade");
			if (!isValidDomainValue(userInfo, connection, "T_FIN", finalidade))
				result.add(new ValidationError("CI062", "infInst", "finalidade", infInstId, finalidade));
			if (infInstValues.get("finalidade") == null)
				result.add(new ValidationError("CI061", "infInst", "finalidade", infInstId));

			// tpAmort
			String tpAmort = (String) infInstValues.get("tpAmort");
			if (!isValidDomainValue(userInfo, connection, "T_TAM", tpAmort))
				result.add(new ValidationError("CI063", "infInst", "tpAmort", infInstId, tpAmort));
			if (StringUtils.isBlank(tpAmort) && tpCaractEspAux.contains("006"))
				result.add(new ValidationError("CI064", "infInst", "tpAmort", infInstId, tpAmort));

			// freqPagam
			if (!isValidDomainValue(userInfo, connection, "T_FPG", (String) infInstValues.get("freqPagam")))
				result.add(new ValidationError("CI065", "infInst", "freqPagam", infInstId,
						(String) infInstValues.get("freqPagam")));

			// divSubor
			if (!isValidDomainValue(userInfo, connection, "T_DIS", (String) infInstValues.get("divSubor")))
				result.add(new ValidationError("CI067", "infInst", "divSubor", infInstId,
						(String) infInstValues.get("divSubor")));

			// instFiduc
			if (!isValidDomainValue(userInfo, connection, "T_FID", (String) infInstValues.get("instFiduc")))
				result.add(new ValidationError("CI068", "infInst", "instFiduc", infInstId,
						(String) infInstValues.get("instFiduc")));

			// montIni
			BigDecimal montIni = (BigDecimal) infInstValues.get("montIni");
			if (montIni == null)
				result.add(new ValidationError("CI085", "infInst", "montIni", infInstId));
			if (montIni != null && montIni.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("CI086", "infInst", "montIni", infInstId, montIni));

			// varFV
			BigDecimal varFV = (BigDecimal) infInstValues.get("varFV");
			if (varFV == null && retrieveSimpleField(connection, userInfo,
					"select from ligInst where tpLigInst='003' and infInst_id = {0} ", new Object[] { infInstId })
							.size() > 0)
				result.add(new ValidationError("CI116", "infInst", "varFV", infInstId));

			// dtReneg
			String tpNeg = (String) infInstValues.get("tpNeg");
			if (dtReneg != null && dtReneg.before(dtIniInst))
				result.add(new ValidationError("CI070", "infInst", "tpNeg", infInstId, tpNeg));
			if (dtReneg != null && dtReneg.after(new Date()))
				result.add(new ValidationError("CI071", "infInst", "tpNeg", infInstId, tpNeg));
			if (dtReneg == null && !StringUtils.equals(tpNeg, "001"))
				result.add(new ValidationError("CI107", "infInst", "tpNeg", infInstId, tpNeg));

			// tpNeg
			if (!isValidDomainValue(userInfo, connection, "T_TNE", tpNeg))
				result.add(new ValidationError("CI072", "infInst", "tpNeg", infInstId, tpNeg));
			if (StringUtils.isBlank(tpNeg))
				result.add(new ValidationError("CI073", "infInst", "tpNeg", infInstId));
			if (StringUtils.isBlank(tpNeg) && dtReneg != null)
				result.add(new ValidationError("CI110", "infInst", "tpNeg", infInstId));

			// percDifCap
			BigDecimal percDifCap = (BigDecimal) infInstValues.get("percDifCap");
			if (percDifCap != null
					&& (percDifCap.compareTo(BigDecimal.ZERO) == -1 || percDifCap.compareTo(new BigDecimal(100)) == 1))
				result.add(new ValidationError("CI074", "infInst", "percDifCap", infInstId, percDifCap));
			if (percDifCap == null && tpCaractEspAux.contains("006"))
				result.add(new ValidationError("CI075", "infInst", "percDifCap", infInstId));

			// tpTitulariz
			String tpTitulariz = (String) infInstValues.get("tpTitulariz");
			if (!isValidDomainValue(userInfo, connection, "T_TTI", tpTitulariz))
				result.add(new ValidationError("CI076", "infInst", "tpTitulariz", infInstId, tpTitulariz));
			if (StringUtils.isBlank(tpTitulariz))
				result.add(new ValidationError("CI096", "infInst", "tpTitulariz", infInstId));

			// seguros
			String seguros = (String) infInstValues.get("seguros");
			if (!isValidDomainValue(userInfo, connection, "T_SAS", seguros))
				result.add(new ValidationError("CI087", "infInst", "seguros", infInstId, seguros));
			if (StringUtils.isBlank(seguros) && tpCaractEspAux.contains("005"))
				result.add(new ValidationError("CI088", "infInst", "seguros", infInstId));

			// lstInfRiscoInst
			List<Integer> infRiscoInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from infRiscoInst where infInst_id = {0} ", new Object[] { infInstId });
			// infRiscoInst
			for (Integer infRiscoInstId : infRiscoInstIdList) {
				HashMap<String, Object> infRiscoInstValues = fillAtributtes(null, connection, userInfo,
						"select * from infRiscoInst where id = {0} ", new Object[] { infRiscoInstId });

				// PDInst
				BigDecimal PDInst = (BigDecimal) infRiscoInstValues.get("PDInst");
				if (PDInst != null && (PDInst.doubleValue() < 0 || PDInst.doubleValue() > 100))
					result.add(new ValidationError("CI077", "infRiscoInst", "PDInst", infRiscoInstId, PDInst));

				// dtPDInst
				Date dtPDInst = (Date) infRiscoInstValues.get("dtPDInst");
				if (dtPDInst != null && dtPDInst.after(new Date()))
					result.add(new ValidationError("CI092", "infRiscoInst", "dtPDInst", infRiscoInstId));

				// tpAvalRiscoInst
				String tpAvalRiscoInst = (String) infRiscoInstValues.get("tpAvalRiscoInst");
				if (!isValidDomainValue(userInfo, connection, "T_TAR",tpAvalRiscoInst))
					result.add(new ValidationError("CI079", "infRiscoInst", "tpAvalRiscoInst", infRiscoInstId, tpAvalRiscoInst));

				// sistAvalRiscoInst
				String sistAvalRiscoInst = (String) infRiscoInstValues.get("sistAvalRiscoInst");
				if (!isValidDomainValue(userInfo, connection, "T_SAR",sistAvalRiscoInst))
					result.add(new ValidationError("CI079", "infRiscoInst", "sistAvalRiscoInst", infRiscoInstId, sistAvalRiscoInst));

				// LGDInst
				BigDecimal LGDInst = (BigDecimal) infRiscoInstValues.get("LGDInst");
				if (LGDInst != null && (LGDInst.doubleValue() < 0 || LGDInst.doubleValue() > 100))
					result.add(new ValidationError("CI078", "infRiscoInst", "LGDInst", infRiscoInstId, LGDInst));

				// tipoPDInst
				String tipoPDInst = (String) infRiscoInstValues.get("tipoPDInst");
				if (!isValidDomainValue(userInfo, connection, "T_TPD", tipoPDInst))
					result.add(new ValidationError("CI101", "infRiscoInst", "tipoPDInst", infRiscoInstId, tipoPDInst));
				if(PDInst!=null && StringUtils.isBlank(tipoPDInst))
					result.add(new ValidationError("CI111", "infRiscoInst", "tipoPDInst", infRiscoInstId));
			}

			// lstLigInst
			List<Integer> ligInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from ligInst where infInst_id = {0} ", new Object[] { infInstId });
			// ligInst
			for (Integer ligInstId : ligInstIdList) {
				HashMap<String, Object> ligInstValues = fillAtributtes(null, connection, userInfo,
						"select * from ligInst where id = {0} ", new Object[] { ligInstId });

				// tpLigInst
				String tpLigInst = (String) ligInstValues.get("tpLigInst");
				if (!isValidDomainValue(userInfo, connection, "T_LIN", tpLigInst))
					result.add(new ValidationError("CI022", "ligInst", "tpLigInst", ligInstId, tpLigInst));
				
				//idEnt
				Integer idEnt_id = (Integer) ligInstValues.get("idEnt_id");
				if(idEnt_id==null && (StringUtils.equals(tpLigInst, "003") || StringUtils.equals(tpLigInst, "004")))
					result.add(new ValidationError("CI024", "ligInst", "idEnt_id", "idEnt", ligInstId, idEnt_id));
				
				//montTransac
				BigDecimal montTransac = (BigDecimal) ligInstValues.get("montTransac");
				if(montTransac==null && !StringUtils.equals(tpLigInst, "003") && !StringUtils.equals(tpLigInst, "004"))
					result.add(new ValidationError("CI026", "ligInst", "montTransac", ligInstId));
			}

			// lstEntSind
			List<Integer> lstEntSindIdList = retrieveSimpleField(connection, userInfo,
					"select id from entSind where infInst_id = {0} ", new Object[] { infInstId });
			if (lstEntSindIdList.isEmpty())
				result.add(new ValidationError("CI018", "infInst", "lstEntSind", infInstId));

			// entSind
			for (Integer entSindId : lstEntSindIdList) {
				HashMap<String, Object> entSindValues = fillAtributtes(null, connection, userInfo,
						"select * from entSind where id = {0} ", new Object[] { entSindId });

				// idEnt
				Integer idEnt_id = (Integer) entSindValues.get("idEnt_id");
				if (retrieveSimpleField(connection, userInfo,
						"select id from entSind where infInst_id = {0} and idEnt_id={1} ",
						new Object[] { infInstId, idEnt_id }).size() > 1)
					result.add(new ValidationError("CI113", "entSind", "idEnt", entSindId));
				
				// relEntsind
				String relEntsind = (String) entSindValues.get("relEntsind");
				if (!isValidDomainValue(userInfo, connection, "T_RPS", relEntsind))
					result.add(new ValidationError("CI016", "entSind", "relEntsind", entSindId, relEntsind));
				if (StringUtils.isBlank(relEntsind))
					result.add(new ValidationError("CI017", "entSind", "relEntsind", entSindId));
			}			
			
		}
		return result;
	}

	private ArrayList<String> validatelstCaracEsp(Connection connection, UserInfoInterface userInfo,
			List<ValidationError> result, Integer infInstId) throws SQLException {
		// lstCaracEsp
		ArrayList<String> tpCaractEspAux = new ArrayList<>();
		Boolean tpCaractEsp000 = false, tpCaractEspxxx = false;
		List<Integer> lstCaracEspIdList = retrieveSimpleField(connection, userInfo,
				"select id from caractEsp where infInst_id = {0} ", new Object[] { infInstId });
		// caractEsp
		for (Integer caractEspId : lstCaracEspIdList) {
			HashMap<String, Object> caractEspValues = fillAtributtes(null, connection, userInfo,
					"select * from caractEsp where id = {0} ", new Object[] { caractEspId });

			// tpCaractEsp
			String tpCaractEsp = (String) caractEspValues.get("tpCaractEsp");
			if (!isValidDomainValue(userInfo, connection, "T_CEP", tpCaractEsp))
				result.add(new ValidationError("CI021", "caractEsp", "tpCaractEsp", caractEspId, tpCaractEsp));

			if (StringUtils.equals("000", tpCaractEsp))
				tpCaractEsp000 = true;
			else if (StringUtils.isNotBlank(tpCaractEsp))
				tpCaractEspxxx = true;

			tpCaractEspAux.add(tpCaractEsp);
		}
		if (tpCaractEsp000 && tpCaractEspxxx)
			result.add(new ValidationError("CI109", "caractEsp", "tpCaractEsp", lstCaracEspIdList.get(0)));

		return tpCaractEspAux;
	}

}
